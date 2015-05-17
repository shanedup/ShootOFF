package com.shootoff.plugins;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.shootoff.camera.CamerasSupervisor;
import com.shootoff.camera.Shot;
import com.shootoff.config.Configuration;
import com.shootoff.config.ConfigurationException;
import com.shootoff.targets.TargetRegion;
import com.shootoff.targets.io.TargetIO;

public class TestShootForScore {
	private PrintStream originalOut;
	private ByteArrayOutputStream stringOut = new ByteArrayOutputStream();
	private PrintStream stringOutStream = new PrintStream(stringOut);
	private List<Group> targets;
	private TargetRegion tenRegion;
	private TargetRegion fiveRegion;
	private ShootForScore sfs;
	
	@Before
	public void setUp() throws ConfigurationException {
		originalOut = System.out;
		System.setOut(stringOutStream);
		
		targets = new ArrayList<Group>();
		Group bullseyeScore = TargetIO.loadTarget(new File("targets" + File.separator + 
				"SimpleBullseye_score.target")).get();
		targets.add(bullseyeScore);
		
		for (Node node : bullseyeScore.getChildren()) {
			TargetRegion region = (TargetRegion)node;
			
			if (region.tagExists("points") && region.getTag("points").equals("10")) {
				tenRegion = region;
			} else if (region.tagExists("points") && region.getTag("points").equals("5")) {
				fiveRegion = region;
			}
		}

		Configuration config = new Configuration(new String[0]);
		config.setDebugMode(true);
		
		sfs = new ShootForScore(targets);
		sfs.init(config, new CamerasSupervisor(config), null);
	}
	
	@After
	public void tearDown() {
		System.setOut(originalOut);
	}
	
	@Test 
	public void testReset() {
		sfs.reset(targets);
		assertEquals("score: 0\n", stringOut.toString());
		stringOut.reset();
	}
	
	@Test 
	public void testJustRed() {
		// Miss
		sfs.shotListener(new Shot(Color.RED, 0, 0, 0, 2), Optional.empty());
		assertEquals("", stringOut.toString());
		stringOut.reset();
		
		// Hit ten
		sfs.shotListener(new Shot(Color.RED, 0, 0, 0, 2), Optional.of(tenRegion));
		assertEquals("red score: 10\n", stringOut.toString());
		stringOut.reset();
		
		// Hit five
		sfs.shotListener(new Shot(Color.RED, 0, 0, 0, 2), Optional.of(fiveRegion));
		assertEquals("red score: 15\n", stringOut.toString());
		stringOut.reset();
		
		assertEquals(15, sfs.getRedScore());
		assertEquals(0, sfs.getGreenScore());
		
		sfs.reset(targets);
		assertEquals("score: 0\n", stringOut.toString());
		stringOut.reset();
		
		assertEquals(0, sfs.getRedScore());
		assertEquals(0, sfs.getGreenScore());
	}
	
	@Test
	public void testJustGreen() {
		// Miss
		sfs.shotListener(new Shot(Color.GREEN, 0, 0, 0, 2), Optional.empty());
		assertEquals("", stringOut.toString());
		stringOut.reset();
		
		// Hit ten
		sfs.shotListener(new Shot(Color.GREEN, 0, 0, 0, 2), Optional.of(tenRegion));
		assertEquals("green score: 10\n", stringOut.toString());
		stringOut.reset();
		
		// Hit five
		sfs.shotListener(new Shot(Color.GREEN, 0, 0, 0, 2), Optional.of(fiveRegion));
		assertEquals("green score: 15\n", stringOut.toString());
		stringOut.reset();
		
		assertEquals(0, sfs.getRedScore());
		assertEquals(15, sfs.getGreenScore());
		
		sfs.reset(targets);
		assertEquals("score: 0\n", stringOut.toString());
		stringOut.reset();
		
		assertEquals(0, sfs.getRedScore());
		assertEquals(0, sfs.getGreenScore());		
	}
	
	
	@Test
	public void testRedAndGreen() {
		// Red hit ten
		sfs.shotListener(new Shot(Color.RED, 0, 0, 0, 2), Optional.of(tenRegion));
		assertEquals("red score: 10\n", stringOut.toString());
		stringOut.reset();
		
		// Green hit five
		sfs.shotListener(new Shot(Color.GREEN, 0, 0, 0, 2), Optional.of(fiveRegion));
		assertEquals("red score: 10\ngreen score: 5\n", stringOut.toString());
		stringOut.reset();
		
		assertEquals(10, sfs.getRedScore());
		assertEquals(5, sfs.getGreenScore());
	}
}
