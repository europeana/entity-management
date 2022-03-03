package eu.europeana.entitymanagement.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.testutils.UnitTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JsonDeserializationTest {

  @Test
  void externalProfileAgentShouldBeDeserializable() throws Exception {
    String agentJson = UnitTestUtils.loadFile(UnitTestUtils.AGENT_EXTERNAL_PROFILE);

    // ObjectMapper with no settings, as this should be deserializable without any additional config
    Agent agent = new ObjectMapper().readValue(agentJson, Agent.class);

    Assertions.assertNotNull(agent);

    // check that Strings are correctly deserialized into List<String>
    assertThat(agent.getDateOfBirth(), not(empty()));
    assertThat(agent.getDateOfDeath(), not(empty()));
    assertThat(agent.getPlaceOfBirth(), not(empty()));
    assertThat(agent.getPlaceOfDeath(), not(empty()));
    assertThat(agent.getGender(), not(empty()));
  }
}
