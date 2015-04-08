(ns leiningen.ldapimem
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [leiningen.core.main :as main])
  (:import [com.unboundid.ldap.listener InMemoryDirectoryServer
                                        InMemoryDirectoryServerConfig
                                        InMemoryListenerConfig]
           [com.unboundid.ldap.sdk.schema Schema]
           [com.unboundid.util.ssl SSLUtil KeyStoreKeyManager TrustStoreTrustManager TrustAllTrustManager]
           [java.util.logging ConsoleHandler]))

(defn- config-value [project k & [default]]
  (get (project :ldapimem) k default))

(defn- create-ldaps-config
  [key-store-path key-store-password trust-store-path secure-port]
  (let [key-store-mgr (KeyStoreKeyManager. key-store-path (.toCharArray key-store-password) "JKS" "localhost")
        trust-store-mgr (TrustStoreTrustManager. trust-store-path)
        ssl-util (SSLUtil. key-store-mgr trust-store-mgr)
        client-ssl-util (SSLUtil. (TrustAllTrustManager.))]
    (InMemoryListenerConfig/createLDAPSConfig "LDAPS"
                                              nil
                                              secure-port
                                              (.createSSLServerSocketFactory ssl-util)
                                              (.createSSLSocketFactory client-ssl-util))))

(defn ldapimem
  "Start an instance of in memory LDAP, run the task, and then stop LDAP."
  [project & args]
  (let [basedn (config-value project :basedn "dc=example,dc=com")
        port (config-value project :port 8389)
        secure-port (config-value project :secure-port 8636)
        username (config-value project :username)
        password (config-value project :password)
        ldif-file-path (config-value project :ldif-file-path)
        schema-file-path (config-value project :schema-file-path)
        noschema? (config-value project :noschema)
        ssl? (config-value project :ssl)
        key-store-path (config-value project :key-store-path "resources/keystore.jks")
        key-store-password (config-value project :key-store-password "password")
        trust-store-path (config-value project :trust-store-path "resources/truststore.jks")
        logging? (config-value project :logging)]
    (println "lein-ldapimem: starting in-memory LDAP instance, port =" port " basedns =" basedn)
    (when ssl?
      (println "lein-ldapimem: starting ssl listener, port =" secure-port))
    (let [config (InMemoryDirectoryServerConfig. (into-array String [basedn]))
          ldap-config (InMemoryListenerConfig/createLDAPConfig "default" port)
          schema (when schema-file-path
                   (Schema/getSchema (into-array String [schema-file-path])))
          ldaps-config (when ssl?
                         (create-ldaps-config key-store-path key-store-password trust-store-path secure-port))]
      (.setListenerConfigs config (into-array InMemoryListenerConfig (if ssl?
                                                                            [ldap-config ldaps-config]
                                                                            [ldap-config])))
      (when (and username password)
        (.addAdditionalBindCredentials config (str "cn=" username) password))
      (when logging?
        (.setAccessLogHandler config (ConsoleHandler.)))
      (if noschema?
        (.setSchema config nil)
        (when schema
          (.setSchema config schema)))
      (let [ldap-server (InMemoryDirectoryServer. config)]
        (when ldif-file-path
          (.importFromLDIF ldap-server true ldif-file-path))
        (.startListening ldap-server)
        (if (seq args)
          (try
            (main/apply-task (first args) project (rest args))
            (finally (.shutDown ldap-server true)))
          (while true (Thread/sleep 5000)))))))
