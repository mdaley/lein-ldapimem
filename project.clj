(defproject lein-ldapimem "0.1.2-SNAPSHOT"
  :description "Creates an in memory LDAP server based on UnboundID's implementation"
  :url "http://github.com/mdaley/lein-ldapimem"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[com.unboundid/unboundid-ldapsdk "2.3.8"]]
  :scm {:name "git"
        :url "https://github.com/mdaley/lein-ldapimem"}
  :eval-in-leiningen true
  :repositories [["releases" {:url "https://clojars.org/repo"
                              :creds :gpg}]]

  :ldapimem {:basedn "dc=example,dc=com"
             :port 8389
             :secure-port 8636
             ;:username "admin"
             ;:password "password"
             :ldif-file-path "resources/data.ldif"
             :noschema true
             :ssl true
             :key-store-path "resources/keystore.jks"
             :trust-store-path "resources/truststore.jks"
             :key-store-password "password"
             :logging true})
