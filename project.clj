(defproject lein-ldapimem "0.1.0-SNAPSHOT"
  :description "Creates an in memory LDAP server based on UnboundID's implementation"
  :url "http://github.com/mdaley/lein-ldapimem"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[com.unboundid/unboundid-ldapsdk "2.3.8"]]
  :scm {:name "git"
        :url "https://github.com/mdaley/lein-ldapimem"}
  :eval-in-leiningen true
  :repositories [["releases" {:url "https://clojars.org/repo"
                              :creds :gpg}]])
