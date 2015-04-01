# lein-ldapimem (LDAP i[n] mem[ory])

A Leiningen 2 plugin that runs a simple embedded instance of unboundID's in-memory LDAP service. Useful for testing purposes.

## Usage

Add `[lein-ldapimem "0.1.0"]` to the `:plugin` vector of your project.

Start the LDAP service when running lein by specifying it before the other tasks that you are running, for example:

    $ lein ldapimem run
    
Once the task completes, the LDAP service will be terminated.

If for some reason you'd like to run the plugin by itself you can invoke it like this, without any further tasks:

    $ lein ldapimem

When you want to stop it just press <kbd>Ctrl</kbd>+<kbd>C</kbd>.

## Configuration

There are optional pieces of configuration that control how the LDAP service operates:

```clojure
(defproject my-project "1.0.0-SNAPSHOT"
  ...
  :plugins [[lein-ldapimem "0.1.0"]]
  ...
  :ldapimem {:port 9839 ;optional - default value is 8389
             :basedn "dc=dev,dc=mycorp,dc=com" ;optional base domain - default is dc=example,dc=com
             :ldif-file-path "ldif.txt; optional - path of file containing valid LDIF data
             :schema-file-path "schema.txt"; optional - path of file containing the schema
             :noschema ; optional - no schema is to be used (not even the default one)
             :logging true} ; optional - default is false
  ...
)
```

## Simple Example

Pick an existing lein project and insert the following into the `project.clj`:

```clojure
:ldapimem {
    :port 8399
    :ldif-file-path "data.ldif"
    :noschema true
    :logging true
  }
```

and insert the plugin into the `:plugins` section of the project definition:

```clojure
:plugins [...
          [lein-ldapimem "0.1.0"]
          ...]}
```

In the root of the project create a file called data.ldif containing:

```
n: dc=example,dc=com
objectClass: domain
objectClass: top
dc: server

dn: cn=Test Entry,dc=example,dc=com
objectClass: inetOrgPerson
cn: Test Entry
sn: Entry
givenName: Test
```

Run your project using lein (I'll just use the task `run` but just use whatever tasks you normally use):

    $ lein ldapimem run

Now you project is running, in another shell type:

    $ ldapsearch -x -h localhost -p 8399

and you will see a response that includes information similar to that specified in `data.ldif` as well as logging information appearing in the running project's console output.

## Notes

* The LDAP service only has one unencrypted listener (which is probably enough for local testing).\
* If you want to run many lein builds in parallel using Jenkins, try the [Port Allocator Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Port+Allocator+Plugin) to avoid port conflicts. If you assign a port to $LDAP_PORT, you can set the `:port` config option for embongo like:

```clojure
(defproject my-project "1.0.0-SNAPSHOT"
  ...
  :ldapimem {
    :port ~(Integer. (get (System/getenv) "LDAP_PORT" 8389)) ;uses 8389 if env variable not set
  ...
```

## License

Copyright © 2015 Matthew Daley

Distributed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html), the same as Clojure.

