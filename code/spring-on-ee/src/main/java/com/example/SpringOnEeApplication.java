package com.example;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

@SpringBootApplication
@EnableJms
public class SpringOnEeApplication {

	public static final String SPRING = "spring";
	public static final String XA_SPRING = "xa-spring";

	public static void main(String[] args) {
		SpringApplication.run(SpringOnEeApplication.class, args);
	}
}

@Component
class MessageProcessor {

	private Log log = LogFactory.getLog(getClass());

	@Autowired
	private GreetingRepository greetingRepository;

	@JmsListener(destination = SpringOnEeApplication.XA_SPRING)
	public void onXaMessage(String msg) {
		log.info("received " + msg);
	}

	@JmsListener(destination = SpringOnEeApplication.SPRING)
	public void onMessage(Map<String, String> msg) {
		log.info("received " + msg);
		Greeting greeting = new Greeting(msg.get("language"),
				msg.get("greeting"));
		this.greetingRepository.save(greeting);
	}
}

interface XaWriteRepository extends JpaRepository<XaWrite, Long> {
}

interface GreetingRepository extends JpaRepository<Greeting, Long> {

	Greeting findByLanguage(String lang);
}

@Component
class GreetingsCommandLineRunner implements CommandLineRunner {

	@Autowired
	private GreetingRepository greetingRepository;

	@Override
	public void run(String... args) throws Exception {
		Stream.of(
				asList(Locale.ENGLISH, "Hello"),
				asList(Locale.FRENCH, "Salut"),
				asList(Locale.GERMAN, "Guten Tag"),
				asList(Locale.ITALIAN, "Buongiorno"))
				.forEach(greetingTuple ->
						greetingRepository.save(new Greeting(
								Locale.class.cast(greetingTuple.get(0)).getLanguage(),
								String.class.cast(greetingTuple.get(1)))));
	}
}

@Entity
class XaWrite {
	@Id
	@GeneratedValue
	private Long id;

	private String message;

	public XaWrite() {
	}

	public XaWrite(String message) {
		this.message = message;
	}

	public Long getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}
}

@Entity
class Greeting {

	@Id
	@GeneratedValue
	private Long id;

	private String greeting;

	private String language;

	public Greeting(String lang, String greeting) {
		this.language = lang;
		this.greeting = greeting;
	}

	Greeting() {// why JPA why???
	}

	public String getLanguage() {
		return language;
	}

	public Long getId() {
		return id;
	}

	public String getGreeting() {
		return greeting;
	}
}


@RestController
class XaWriteRestController {

	@Autowired
	private XaWriteRepository xaWriteRepository;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Transactional
	@RequestMapping(method = RequestMethod.GET, value = "/xa/{fail}")
	public void xaWrite(@PathVariable boolean fail) {
		String failMessage = (fail ? "" : "not ") + "failing to write @ " + Instant.now().toString();

		this.jmsTemplate.convertAndSend(SpringOnEeApplication.XA_SPRING, failMessage);
		this.xaWriteRepository.save(new XaWrite(failMessage));

		if (fail) throw new RuntimeException(failMessage);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/xa")
	public Collection<XaWrite> xaWrites() {
		return this.xaWriteRepository.findAll();
	}

}

@RestController
@RequestMapping(value = "/greetings")
class GreetingsRestController {

	@Autowired
	private GreetingRepository greetingRepository;

	@Autowired
	private JmsTemplate jmsTemplate;

	// curl -H"Content-Type: application/json" -XPOST -d'{  "language" : "你好" , "greeting" : "CN"}'  http://localhost:8080/mvc-0.0.1-SNAPSHOT/greetings
	@RequestMapping(method = RequestMethod.POST)
	public void postGreeting(@RequestBody Greeting greeting) {
		Map<String, Object> msg = new HashMap<>();
		msg.put("greeting", greeting.getGreeting());
		msg.put("language", greeting.getLanguage());
		this.jmsTemplate.convertAndSend(SpringOnEeApplication.SPRING, msg);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/languages/{language}")
	public Greeting getGreetingByLanguage(@PathVariable String language) {
		return this.greetingRepository.findByLanguage(language);
	}

	@RequestMapping(method = RequestMethod.GET)
	public Collection<Greeting> getGreetings() {
		return this.greetingRepository.findAll();
	}
}
