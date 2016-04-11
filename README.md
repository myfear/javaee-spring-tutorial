# Integration architecture with Java EE and Spring

Markus Eisele (Lightbend), Josh Long (Pivotal)

1:30pm–5:00pm Monday, 04/11/2016

Integration architecture

Location: Sutton

O'Reilly Software Architecture Conference NYC 2016
[tutorial website](http://conferences.oreilly.com/software-architecture/engineering-business-us/public/schedule/detail/48226)


## Preparation

This section describes what, how, and where to install the software needed for this lab.

### Hardware
Operating System: Windows 7 (SP1), Mac OS X (10.8 or later), Fedora (21 or later)
Memory: At least 4 GB+, preferred 8 GB

### Software

* Java: [Oracle JDK 8u45 or later](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* Web Browser: [Chrome](https://www.google.com/chrome/browser/desktop/), [Firefox](http://www.getfirefox.com),
* [WildFly 10.0.0.Final](http://wildfly.org/downloads/)
* [Eclipse](http://www.eclipse.org/downloads/) (or IDE of choice)

### Maven

* Download Apache Maven [from the Internet](https://maven.apache.org/download.cgi)
* Unzip to a directory of your choice and add it to the `PATH`.

### Install and configure WildFly
WildFly 10 is the latest release in a series of JBoss open-source application server offerings.  WildFly 10 is an exceptionally fast, lightweight and powerful implementation of the Java Enterprise Edition 7 Platform specifications.  The state-of-the-art architecture built on the Modular Service Container enables services on-demand when your application requires them.

Simply extract your chosen download to the directory of your choice (we call it +%JBOSS_HOME+). You can install WildFly 10 on any operating system that supports the zip or tar formats. Refer to the Release Notes for additional information related to the release.

Change directory to $JBOSS_HOME/bin.

You're going to need to run it using the _full_, not _web_, profile configuration. For the easiest experience, rename  `$JBOSS_HOME/bin/wildfly/standalone/configuration/standalone-full.xml` to `$JBOSS_HOME/bin/wildfly/standalone/configuration/standalone.xml`.

Open the `standalone.xml` and find `urn:jboss:domain:messaging-activemq:1.0`. There, you'll see a `server` element and inside that a `security-setting` element. Let's make life easier for ourselves and disable security for this lab. Add the following ABOVE the `security-setting` element:

```
<security enabled="false"/>
<cluster password="${jboss.messaging.cluster.password:guest}"/>
```

Add two `queue`s to work with for our example:

```
<jms-queue name="spring" entries="java:/jms/queue/spring"/>
<jms-queue name="xa-spring" entries="java:/jms/queue/xa-spring"/>
```

Save your changes and then, in the `$JBOSS_HOME/bin` directory, start Wildfly:

```sh
./standalone.sh
```

Open another console and add a management user:
```sh
./add-user.sh
```

* a) Management user
* User: "admin"
* Password: "admin"
* Answer "yes" (don't do this in production)
* Re-enter Password: "admin"
* hit return (no groups)
* enter "no"

As with previous WildFly releases, you can point your browser to http://localhost:8080 (if using the default configured HTTP port) which brings you to the Welcome Screen.

If you run into problems, refer to the complete [getting started guide and verify](https://docs.jboss.org/author/display/WFLY10/Getting+Started+Guide) that you completed all necessary steps.
