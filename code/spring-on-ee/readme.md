# Getting Started with Wildfly

* Download [Wildfly 10](http://download.jboss.org/wildfly/10.0.0.Final/wildfly-10.0.0.Final.zip)
* Do _not_ start it up, yet.
* setup an environment variable, `WILDFLY_HOME`, and point it to where you unzipped Wildfly. On my machine, I chose `$HOME/bin/wildfly`
* You're going to need to run it using the _full_ profile configuration. For the easiest experience, rename  `$WILDFLY_HOME/bin/wildfly/standalone/configuration/standalone-full.xml` to `$WILDFLY_HOME/bin/wildfly/standalone/configuration/standalone.xml`.
* open the `standalone.xml` and find `urn:jboss:domain:messaging-activemq:1.0`. There, you'll see a `server` element and inside that a `security-setting` element. Let's make life easier for ourselves and disable security for this lab. Add the following inside the `security-setting` element:
```
  <security enabled="false"/>
  <cluster password="${jboss.messaging.cluster.password:guest}"/>
```
* Add two `queue`s to work with for our example:
```
<jms-queue name="spring" entries="java:/jms/queue/spring"/>
<jms-queue name="xa-spring" entries="java:/jms/queue/xa-spring"/>
```
* Save the file.
