/*
 *   Copyright 2015 Calytrix Technologies
 *
 *   This file is part of hperf.
 *
 *   NOTICE:  All information contained herein is, and remains
 *            the property of Calytrix Technologies Pty Ltd.
 *            The intellectual and technical concepts contained
 *            herein are proprietary to Calytrix Technologies Pty Ltd.
 *            Dissemination of this information or reproduction of
 *            this material is strictly forbidden unless prior written
 *            permission is obtained from Calytrix Technologies Pty Ltd.
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package hperf;

import static hperf.Handles.AC_CREATOR;
import static hperf.Handles.AC_FEDERATE_NAME;
import static hperf.Handles.AC_PAYLOAD;
import static hperf.Handles.IC_PING;
import static hperf.Handles.IC_PING_ACK;
import static hperf.Handles.IC_THROUGHPUT;
import static hperf.Handles.OC_TEST_FEDERATE;
import static hperf.Handles.OC_TEST_OBJECT;
import static hperf.Handles.PC_PING_ACK_PAYLOAD;
import static hperf.Handles.PC_PING_ACK_SENDER;
import static hperf.Handles.PC_PING_ACK_SERIAL;
import static hperf.Handles.PC_PING_PAYLOAD;
import static hperf.Handles.PC_PING_SENDER;
import static hperf.Handles.PC_PING_SERIAL;
import static hperf.Handles.PC_THROUGHPUT_PAYLOAD;
import static hperf.Handles.PC_THROUGHPUT_SENDER;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.cmabreu.services.FederateService;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.RTIexception;
import hperf.config.Configuration;
import hperf.latency.LatencyDriver;
import hperf.lifecycle.LifecycleDriver;
import hperf.throughput.ThroughputDriver;



public class TestRunner
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Configuration configuration;
	
	private RTIambassador rtiamb;
	private FederateAmbassador fedamb;
	private Storage storage;
	private IDriver driver;
	private Logger logger = LoggerFactory.getLogger( TestRunner.class );

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public TestRunner( Configuration configuration )
	{
		// logging and configuration
		this.configuration = configuration;
		initializeLogging();

		this.storage = new Storage();
		this.rtiamb = null; // created during createAndJoinFederate()
		this.fedamb = new FederateAmbassador( configuration, storage );
		this.driver = null; // created during execute()
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	// main test federate execution process
	public void execute( URL[] modules ) throws Exception
	{
		// load the appropriate driver
		this.driver = loadDriver( configuration, storage );
		
		logger.info( "" );
		logger.info( "    Test Driver: "+driver.getName() );
		logger.info( "  Callback Mode: "+(configuration.isImmediateCallback() ? "IMMEDIATE":"EVOKED") );
		logger.info( "   Time Stepped: "+(configuration.isTimestepped() ? "YES":"NO") );
		logger.info( "" );

		// if the whole test should be run on a loop, do things a little differently, otherwise,
		// just go with a single execution of the lifecycle
		if( driver.manageLifecycleManually() )
			executeUnmanaged();
		else
			executeManaged( modules );
	}

	/**
	 * This executes the driver with managed lifecycle. Federation creation, joining, publication
	 * and subscription, initial object registration and a wait for peers are all executed by the
	 * runner rather than the driver. 
	 */
	private void executeManaged( URL[] modules ) throws Exception
	{
		// Create and Join the federation
		TestFederate federate = this.createAndJoinFederation( modules );
		storage.setLocalFederate( federate );

		// Publish and Subscribe
		this.publishAndSubscribe();
		
		// Register object representing federate
		this.registerFederateObject();
		
		// Print out some test information and wait for our peers to turn up
		driver.printWelcomeMessage();

		// Wait until everyone else turns up
		this.waitForPeers();

		// Announce all the sync points up front -- life is just easier this way
		this.announceSyncPoints();
		
		/////////////////////////
		// HPerfMain test execution //
		/////////////////////////
		// this.driver.execute( rtiamb, fedamb );

		// Get out of here
		// this.resignAndDestroy();
	}
	
	
	public void inicia() throws Exception {
		this.driver.execute( rtiamb, fedamb );
	}
	
	public void quit() throws Exception {
		this.resignAndDestroy();
	}
	
	
	
	
	
	
	
	
	
	
	

	/**
	 * This executes the driver and leaves the lifecycle management entirely to it.
	 * `TestFederate` and `Storage` objects are constructed, but the full management
	 * of federation lifecycle (from create down) is left to the Driver, with the
	 * `execute()` method called directly to let it do its thing.
	 * 
	 * NOTE: Because nothing will have been set up, there is no RTIambassador or FederateAmbassador
	 *       to be given to `execute()`, so `null` is passed for both parameters. 
	 */
	private void executeUnmanaged() throws Exception
	{
		// Create the TestFederate class and wire it to the storage object
		TestFederate federate = new TestFederate( configuration.getFederateName(), true );
		storage.setLocalFederate( federate );

		logger.info( "Passing execution to the Driver: "+this.driver.getName() );
		
		// Let it run. RUN FREE LITTLE ONE
		this.driver.execute( null, fedamb );
		
		// Print a message
		logger.info( "Driver has completed execution, exiting." );
	}
	
	/**
	 * Set up a default logger with a basic logging pattern.
	 */
	private void initializeLogging(){	
		
	}

	private IDriver loadDriver( Configuration configuration, Storage storage ) throws Exception
	{
		IDriver driver = null;
		
		if( configuration.isThroughputTestEnabled() )
			driver = new ThroughputDriver();
		else if( configuration.isLatencyTestEnabled() )
			driver = new LatencyDriver();
		else if( configuration.isLifecycleTestEnabled() )
			driver = new LifecycleDriver();
		else
			throw new Exception( "You must specify at least --throughput-test or --latency-test" );
		
		// configure the driver
		driver.configure( configuration, storage );
		return driver;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Federate Lifecycle Methods ////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Create and join the federation. Note that someone else may have gotten in first, so we
	 * should expect that the federation creation process will fail.
	 */
	private TestFederate createAndJoinFederation( URL[] modules ) throws RTIexception
	{
		/////////////////////////////////////////////////
		// 1 & 2. create the RTIambassador and Connect //
		/////////////////////////////////////////////////
		logger.info( "Creating RTIambassador" );
		this.rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
		
		// connect
		logger.info( "Connecting..." );
		CallbackModel cbmodel = configuration.isImmediateCallback() ? CallbackModel.HLA_IMMEDIATE :
		                                                              CallbackModel.HLA_EVOKED;
		rtiamb.connect( fedamb, cbmodel );

		//////////////////////////////
		// 3. create the federation //
		//////////////////////////////
		logger.info( "Creating Federation..." );
		// We attempt to create a new federation with the first three of the
		// restaurant FOM modules covering processes, food and drink
		try
		{
			rtiamb.createFederationExecution( configuration.getFederationName(), modules );
			logger.info( "Created Federation ["+configuration.getFederationName()+"]" );
		}
		catch( FederationExecutionAlreadyExists exists )
		{
			logger.warn( "Didn't create federation, it already existed" );
		}

		////////////////////////////
		// 4. join the federation //
		////////////////////////////
		rtiamb.joinFederationExecution( configuration.getFederateName(),
		                                configuration.getFederateName(),
		                                configuration.getFederationName() );

		logger.info( "Joined Federation as " + configuration.getFederateName() );

		////////////////////////
		// 5. enable callback //
		////////////////////////
		// because REASONS, le sigh
		rtiamb.enableCallbacks();
		rtiamb.enableAsynchronousDelivery();

		return new TestFederate( configuration.getFederateName(), true/*local*/ );
	}

	/**
	 * Cache all handles and perform publication and subscription for all object and
	 * interaction types we require to complete throughput and latency tests.
	 */
	private void publishAndSubscribe() throws RTIexception
	{
		// Cache up all the handles
		OC_TEST_FEDERATE  = rtiamb.getObjectClassHandle( "TestFederate" );
		AC_FEDERATE_NAME = rtiamb.getAttributeHandle( OC_TEST_FEDERATE, "federateName" );

		OC_TEST_OBJECT  = rtiamb.getObjectClassHandle( "TestObject" );
		AC_CREATOR      = rtiamb.getAttributeHandle( OC_TEST_OBJECT, "creator" );
		AC_PAYLOAD      = rtiamb.getAttributeHandle( OC_TEST_OBJECT, "payload" );
		
		IC_THROUGHPUT         = rtiamb.getInteractionClassHandle( "ThroughputInteraction" );
		PC_THROUGHPUT_SENDER  = rtiamb.getParameterHandle( IC_THROUGHPUT, "sender" );
		PC_THROUGHPUT_PAYLOAD = rtiamb.getParameterHandle( IC_THROUGHPUT, "payload" );

		IC_PING         = rtiamb.getInteractionClassHandle( "Ping" );
		PC_PING_SERIAL  = rtiamb.getParameterHandle( IC_PING, "serial" );
		PC_PING_SENDER  = rtiamb.getParameterHandle( IC_PING, "sender" );
		PC_PING_PAYLOAD = rtiamb.getParameterHandle( IC_PING, "payload" );

		IC_PING_ACK         = rtiamb.getInteractionClassHandle( "PingAck" );
		PC_PING_ACK_SERIAL  = rtiamb.getParameterHandle( IC_PING_ACK, "serial" );
		PC_PING_ACK_SENDER  = rtiamb.getParameterHandle( IC_PING_ACK, "sender" );
		PC_PING_ACK_PAYLOAD = rtiamb.getParameterHandle( IC_PING_ACK, "payload" );
		
		///////////////////////////
		// Publish and Subscribe //
		///////////////////////////
		// Class: TestFederate
		AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();
		attributes.add( AC_FEDERATE_NAME );
		rtiamb.publishObjectClassAttributes( OC_TEST_FEDERATE, attributes );
		rtiamb.subscribeObjectClassAttributes( OC_TEST_FEDERATE, attributes );
		
		// Class: TestObject
		attributes.clear();
		attributes.add( AC_CREATOR );
		attributes.add( AC_PAYLOAD );
		rtiamb.publishObjectClassAttributes( OC_TEST_OBJECT, attributes );
		rtiamb.subscribeObjectClassAttributes( OC_TEST_OBJECT, attributes );

		// Class: ThroughputInteraction
		rtiamb.publishInteractionClass( IC_THROUGHPUT );
		rtiamb.subscribeInteractionClass( IC_THROUGHPUT );

		// Class: LatencyInteraction
		rtiamb.publishInteractionClass( IC_PING );
		rtiamb.subscribeInteractionClass( IC_PING );
		rtiamb.publishInteractionClass( IC_PING_ACK );
		rtiamb.subscribeInteractionClass( IC_PING_ACK );
		
		logger.info( "Publish and Subscribe complete" );
	}

	/**
	 * Register and update an object representing this federate so that others may discover us.
	 * This currently covers up for the lack of MOM support in the Portico 1516e interface.
	 */
	private void registerFederateObject() throws RTIexception
	{
		logger.info( "Registering HLAobjectRoot.TestFederate object for local federate" );
		rtiamb.registerObjectInstance( OC_TEST_FEDERATE, configuration.getFederateName() );
	}
	
	/**
	 * This method ticks the RTI until we've received word that all federates listed
	 * as peers are present. 
	 */
	private void waitForPeers() throws RTIexception
	{
		///////////////////////////////////////////
		// wait for all the federates to connect //
		///////////////////////////////////////////
		logger.info( "Waiting for peers: "+configuration.getPeers() );
		List<String> absentPeers = new ArrayList<String>( configuration.getPeers() );
		absentPeers.remove( storage.getLocalFederate() );

		while( absentPeers.isEmpty() == false )
		{
			// let the RTI work for a bit while we wait to discover the
			// objects registered by the remote federates
			if( configuration.isImmediateCallback() )
				Utils.sleep( 500 );
			else
				rtiamb.evokeMultipleCallbacks( 1.0, 1.0 );

			// check to see who turned up
			for( TestFederate federate : storage.getPeers() )
			{
				String federateName = federate.getFederateName();
				if( absentPeers.contains(federateName) )
				{
					absentPeers.remove( federateName );
					logger.debug( "  ... found "+federateName );
				}
			}
		}

		logger.info( "All federates present - let's do this thing!" );
	}

	/**
	 * Announce all our sync points up front so that they're all ready for when we
	 * need them later.
	 */
	private void announceSyncPoints()
	{
		String[] points = new String[] { "START_THROUGHPUT_TEST",
		                                 "FINISH_THROUGHPUT_TEST",
		                                 "START_LATENCY_TEST",
		                                 "FINISH_LATENCY_TEST" };

		for( String point : points )
		{
			try
			{
				rtiamb.registerFederationSynchronizationPoint( point, new byte[]{} );
				rtiamb.evokeMultipleCallbacks(0.1,0.1); // let the success/fail callback come in
			}
			catch( Exception e )
			{
				logger.warn( "Exception registering sync point ["+point+"]: "+e.getMessage() );
			}			
		}
	}
	
	/**
	 * Leave the federation. On the way out, try to destroy the federation. If we happen
	 * to be the last one out this is just a common cleanup courtesy
	 */
	private void resignAndDestroy() throws RTIexception
	{
		////////////////////////////////
		// resign from the federation //
		////////////////////////////////
		rtiamb.resignFederationExecution( ResignAction.DELETE_OBJECTS );
		logger.info( "Resigned from Federation" );

		////////////////////////////////////
		// try and destroy the federation //
		////////////////////////////////////
		// NOTE: we won't die if we can't do this because other federates
		//       remain. in that case we'll leave it for them to clean up
		try
		{
			rtiamb.destroyFederationExecution( configuration.getFederationName() );
			logger.info( "Destroyed Federation" );
		}
		catch( FederationExecutionDoesNotExist dne )
		{
			logger.info( "No need to destroy federation, it doesn't exist" );
		}
		catch( FederatesCurrentlyJoined fcj )
		{
			logger.info( "Didn't destroy federation, federates still joined" );
		}

		/////////////////////////////
		// disconnect from the RTI //
		/////////////////////////////
		// This lets the LRC clean things up
		rtiamb.disconnect();
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
