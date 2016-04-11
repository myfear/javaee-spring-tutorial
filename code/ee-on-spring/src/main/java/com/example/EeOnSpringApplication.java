package com.example;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSRuntimeException;
import javax.jms.Queue;
import javax.persistence.*;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collection;

@SpringBootApplication
public class EeOnSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(EeOnSpringApplication.class, args);
	}

	@Entity
	public static class Greeting {

		@Id
		@GeneratedValue
		private Long id;

		private String greeting;

		Greeting() {
		}

		public Greeting(String greeting) {
			this.greeting = greeting;
		}

		public Long getId() {
			return id;
		}

		public String getGreeting() {
			return greeting;
		}
	}


	@Named
	@Transactional
	public static class XaGreetingService {

		private Log log = LogFactory.getLog(getClass());


		@PersistenceContext
		private EntityManager entityManager;

		@Inject
		private ConnectionFactory connectionFactory;

		public Collection<Greeting> readGreetings() {
			return this.entityManager.createQuery(
					"select g from " + Greeting.class.getName() + " g", Greeting.class).getResultList();
		}

		public void createGreeting(Greeting greeting) {
			this.entityManager.merge(greeting);
			try (JMSContext context = connectionFactory.createContext()) {
				Queue spring = context.createQueue("spring");
				context.createProducer().send( spring, greeting.getGreeting());
			}
			catch (JMSRuntimeException ex) {
				this.log.warn(ex);
			}
		}
	}

	@Named
	@Path("/greetings")
	@Consumes("application/json")
	@Produces("application/json")
	public static class GreetingEndpoint {

		@Inject
		private XaGreetingService xaGreetingService;

		@GET
		public Collection<Greeting> read() {
			return xaGreetingService.readGreetings();
		}

		@POST
		public void create(MultivaluedMap<String, String> body) {
			this.xaGreetingService.createGreeting(new Greeting(body.getFirst("greeting")));
		}
	}

	@Named
	public static class JaxrsApplicationConfig extends ResourceConfig {

		public JaxrsApplicationConfig() {
			this.register(GreetingEndpoint.class);
			this.register(JacksonFeature.class);
		}
	}
}

