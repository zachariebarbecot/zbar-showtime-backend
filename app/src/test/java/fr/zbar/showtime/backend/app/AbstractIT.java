package fr.zbar.showtime.backend.app;

import fr.zbar.starter.test.ZbarSpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.test.web.servlet.client.RestTestClient;

@ZbarSpringBootTest
@AutoConfigureRestTestClient
abstract class AbstractIT {

  @Autowired protected RestTestClient restTestClient;
}
