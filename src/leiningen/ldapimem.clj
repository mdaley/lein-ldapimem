(ns leiningen.ldapimem
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [leiningen.core.main :as main])
  (:import [com.unboundid.ldap.listener InMemoryDirectoryServer InMemoryDirectoryServerConfig InMemoryListenerConfig]
           [com.unboundid.ldap.sdk.schema Schema]
           [java.util.logging ConsoleHandler]))

(defn- config-value [project k & [default]]
  (get (project :ldapimem) k default))

(defn ldapimem
  "Start an instance of in memory LDAP, run the task, and then stop LDAP."
  [project & args]
  (let [basedn (config-value project :basedn "dc=example,dc=com")
        port (config-value project :port 8389)
        ldif-file-path (config-value project :ldif-file-path)
        schema-file-path (config-value project :schema-file-path)
        noschema? (config-value project :noschema)
        logging? (config-value project :logging)]
    (println "lein-ldapimem: starting in-memory LDAP instance, port =" port " basedns =" basedn)
    (let [ldap-config (InMemoryDirectoryServerConfig. (into-array String [basedn]))
          default-listener (InMemoryListenerConfig/createLDAPConfig "default" port)
          schema (when schema-file-path
                   (Schema/getSchema (into-array String [schema-file-path])))]
      (.setListenerConfigs ldap-config (into-array InMemoryListenerConfig [default-listener]))
      (when logging?
        (.setAccessLogHandler ldap-config (ConsoleHandler.)))
      (if noschema?
        (.setSchema ldap-config nil)
        (when schema
          (.setSchema ldap-config schema)))
      (let [ldap-server (InMemoryDirectoryServer. ldap-config)]
        (when ldif-file-path
          (.importFromLDIF ldap-server true ldif-file-path))
        (.startListening ldap-server)
        (if (seq args)
          (try
            (main/apply-task (first args) project (rest args))
            (finally (.shutDown ldap-server true)))
          (while true (Thread/sleep 5000)))))))
