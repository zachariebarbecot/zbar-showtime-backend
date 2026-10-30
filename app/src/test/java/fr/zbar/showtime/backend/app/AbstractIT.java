package fr.zbar.showtime.backend.app;

import fr.zbar.starter.test.ZbarSpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

@ZbarSpringBootTest
@AutoConfigureRestTestClient
@Import(FixedClockConfiguration.class)
@ActiveProfiles("test")
abstract class AbstractIT {

  @Autowired protected RestTestClient restTestClient;
}
