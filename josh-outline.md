# J & S

* preamble
Who are we? WHy the fuck should you care?

Markus: lots of EE experience
Josh: lots of Spring experience
Both: beer and code and coffee

* Intro



## then vs now: Java EE
1. economized CLASSPATHs and shared platform libraries and services
2. shared, common, baseline contracts for slow moving technologies (messaging w/ JMS, SQL w/ JDBC, HTTP w Servlets)
3. centralized governance aware of the J2EE deployer roles and typical, slow, change management processes
4. innovation through implementations
5. convention over configuration

1. not relevant. Nobody cares about 40MB of shared LIBs. new languages like Go don't even support dynamic link libraries because they cause more problems than they solve
2. absolutely still a good idea. problem is when you standardize things where there is a lot of room for innovation (like persistence, web frameworks, microservices, social integration, NoSQL, security, and even component models)
3. this doesn't exist anymore: devops vs (dev. VS ops). We're not throwing work over the wall anymore. These 'deployer roles' are all handled by the same person. The forced schism is arbitrary and creates friction. Most orgs want CI/CD. they want to evolve the app along with its services in lockstep and guarantee reproducible builds. Having to check in an AS in its entirety to guarantee this is silly.
4. still interesting: Oracle vs MS Access? Whichever is the most secure, stable, performant, pick that! code doesn't change
5. same, even more

## then vs now: Spring
1. Spring was born to simplify J2EE APIs
2. Spring was born to promote testing, faster feedback loops
3. to provide patterns and best practices
4. provide flexibility through configuration (over convention)
5.



## reality today

* JavaEE first

* Spring First
