package no.fint.consumer.models

import no.fint.consumer.status.StatusCache
import no.fint.consumer.utils.RestEndpoints
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.core.io.ClassPathResource
import org.springframework.http.*
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = ["logging.level.no.fint.consumer.models=TRACE"])
@ActiveProfiles("test")
class ElevControllerSpec extends Specification {
    @LocalServerPort
    private int port

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private StatusCache statusCache

    def "PUT elev.json and GET /status/"() {
        given:
        String content = IOUtils.toString(new ClassPathResource("elev.json").getInputStream(), "UTF-8")
        HttpHeaders headers = new HttpHeaders()
        headers.add("x-org-id", "pwf.no")
        headers.add("x-client", "test")
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8)

        when:
        ResponseEntity<String> result = restTemplate.exchange("http://localhost:{port}{endpoint}/elevnummer/500001", HttpMethod.PUT, new HttpEntity<>(content, headers), String.class, port, RestEndpoints.ELEV)
        println("result = " + result)

        then:
        result.getStatusCode().is2xxSuccessful()
        result.getHeaders().getLocation()

        when:
        def location = UriComponentsBuilder.fromUri(result.getHeaders().getLocation()).port(port).build().toUri()
        result = restTemplate.exchange(location, HttpMethod.GET, new HttpEntity<>(null, headers), String.class)
        println("result = " + result)

        then:
        result.getStatusCode() == HttpStatus.ACCEPTED

        when:
        def corrid = location.path.substring(location.path.lastIndexOf('/') + 1)
        def event = statusCache.get(corrid)
        println(corrid)
        println(event.data)

        then:
        event
        event.data[0].feidenavn.identifikatorverdi == "test@sundetvgs.haugfk.no"
    }
}
