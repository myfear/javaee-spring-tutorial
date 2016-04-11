package com.example;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.ConnectionFactory;
import javax.persistence.*;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import java.time.Instant;
import java.util.Collection;

@SpringBootApplication
@EnableJms
public class EeOnSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(EeOnSpringApplication.class, args);
	}

	// <spring>
	@Named
	public static class GreetingMessageListener {

		private Log log = LogFactory.getLog(getClass());

		@JmsListener(destination = "spring")
		public void processGreeting(String greetingMessage) {
			this.log.info("received " + greetingMessage);
		}
	}
	// </spring>

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

	/*	// pure JMS 1.1
		private void sendMessage(String text) throws JMSException {
			Connection connection = null;
			try {
				connection = connectionFactory.createConnection();
				Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
				Queue queue = session.createQueue("spring");
				MessageProducer messageProducer = session.createProducer(queue);
				TextMessage textMessage = session.createTextMessage(text);
				messageProducer.send(textMessage);
			} catch (JMSException ex) {
				log.warn(ex);
			} finally {
				if (null != connection) {
					connection.close();
				}
			}
		}*/

		// <spring>
		@Inject
		private JmsTemplate jmsTemplate;

		private void sendMessage(String text) {
			this.jmsTemplate.convertAndSend("spring", text);
		}
		// </spring>

		public void createGreeting(Greeting greeting, boolean fail) throws Exception {
			this.createGreeting(greeting);
			if (fail) {
				throw new RuntimeException("failing @ " + Instant.now().toString());
			}
		}

		public void createGreeting(Greeting greeting) throws Exception {
			this.entityManager.merge(greeting);
			this.sendMessage(greeting.getGreeting());
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

		// curl -H "Content-Type: application/json" -d"{\"greeting\" : \"Hi\"}"  http://localhost:8080/greetings?fail=false
		@POST
		public void create(@QueryParam("fail") boolean fail,
		                   Greeting body) throws Exception {
			this.xaGreetingService.createGreeting(body, fail);
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

