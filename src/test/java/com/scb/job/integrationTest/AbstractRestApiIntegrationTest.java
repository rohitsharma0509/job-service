package com.scb.job.integrationTest;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.job.repository.JobRepository;
import com.scb.job.service.proxy.EstimatePriceProxy;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ActiveProfiles(value = "test")
@SpringBootTest
public class AbstractRestApiIntegrationTest {

  @Autowired
  protected MockMvc mockMvc;
  @Autowired
  protected ObjectMapper objectMapper;
  @Autowired
  protected JobRepository jobRepository;
  @Autowired
  protected EstimatePriceProxy estimatePriceProxy;

  private static ClientAndServer mockServer;

  private static final String CONNECTION_STRING = "mongodb://%s:%d";

  private static MongodExecutable mongodExecutable;

  @AfterEach
  void cleanUpDatabase() {
    jobRepository.deleteAll();

  }

  @BeforeAll
  public static void startMockServer() throws IOException {
    mockServer = startClientAndServer(1080);
    BaseMockSetup.setUp(mockServer);

    String ip = "localhost";
    int port = 64096;

    IMongodConfig mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
        .net(new Net(ip, port, Network.localhostIsIPv6()))
        .build();

    MongodStarter starter = MongodStarter.getDefaultInstance();
    mongodExecutable = starter.prepare(mongodConfig);
    mongodExecutable.start();
  }

  @AfterAll
  public static void stopMockServer() {
    mongodExecutable.stop();
    if (mockServer.isRunning()) {
      mockServer.stop();
    }
  }


}

