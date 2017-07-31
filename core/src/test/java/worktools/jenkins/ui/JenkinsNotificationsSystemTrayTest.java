package worktools.jenkins.ui;

import static org.junit.Assert.assertNotNull;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JenkinsNotificationsSystemTrayTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		assertNotNull(loadImage("success.png"));
	}
	
	Image loadImage(String imageFileName) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(imageFileName);
		try {
			return ImageIO.read(resource);
		} catch (IOException e) {
			throw new RuntimeException("could not load image", e);
		}
	}

}
