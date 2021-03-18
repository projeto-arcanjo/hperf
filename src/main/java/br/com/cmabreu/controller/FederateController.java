package br.com.cmabreu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.cmabreu.services.FederateService;

@RestController
public class FederateController {
	
    @Autowired
    private FederateService federateService;	
	
    
    /*    
	echo "usage: throughput.sh [--arg <value>]"
	echo ""
	echo "    --federate-name      [stirng]    (REQUIRED) Name for this federate"
	echo "    --federation-name    [string]    (optional) Name of the federation we're joining, default hperf"
	echo "    --loops              [number]    (optional) Number of loops we should iterate for, default 20"
	echo "    --peers              [list]      (REQUIRED) Comma-separated list of other federate names"
	echo "    --packet-size        [number]    (optional) Min size of messages. e.g. 1B, 1K, 1M, default 1K"
	echo "    --validate-data                  (optional) Validate received contents and log any errors, default false"
	echo "    --callback-immediate             (optional) Use the immediate callback HLA mode (default)"
	echo "    --callback-evoked                (optional) If specified, used the ticked HLA callback mode"
	echo "    --loop-wait          [number]    (optional) How long to tick (ms) each loop (if in 'evoked' mode), default 10"
	echo "                                                This argument has no effect in immediate callback mode"
	echo "    --log-level          [string]    (optional) TRACE | DEBUG | INFO | WARN | FATAL | OFF, default INFO"
	echo "    --print-interval     [number]    (optional) Print status update every X iterations, default 10% of loops"
	echo "    --print-megabits                 (optional) If specified, prints throughput in megabits-per-second"
	echo ""
*/	
    
    
/*    

	echo ""
	echo "The latency test is run with any number of federates. Only one federate is"
	echo "designated as the sender. The others are all receivers/responders. The    "
	echo "sender instigates each loops by sending a Ping interaction, to which all  "
	echo "responders reply with a PingAck"
	echo ""
	echo " NOTE: Bundling can have an adverse effect on latency performance. Turn it"
	echo "       off in the RTI.rid file before continuing."
	echo ""
	echo "usage: latency.sh [--arg <value>]"
	echo ""
	echo "    --federate-name      [stirng]    (REQUIRED) Name for this federate"
	echo "    --federation-name    [string]    (optional) Name of the federation we're joining, default hperf"
	echo "    --loops              [number]    (optional) Number of loops we should iterate for, default 20"
	echo "    --peers              [list]      (REQUIRED) Comma-separated list of other federate names"
	echo "    --packet-size        [number]    (optional) Min size of messages. e.g. 1B, 1K, 1M, default 1K"
	echo "    --sender                         (optional) Is this federate the one event sender, default false"
	echo "    --validate-data                  (optional) Validate received contents and log any errors, default false"
	echo "    --log-level          [string]    (optional) TRACE | DEBUG | INFO | WARN | FATAL | OFF, default INFO"
	echo ""
	echo "example: ./latency-sh --federate-name one --peers two,three --loops 10000 --sender"
	echo ""
	exit;

*/    
  
    
/*    
	echo "The lifecycle test is typically run with two federates. One that is the   "
	echo "sender and will constantly join and resign. The other is the responder.   "
	echo "They will stay present in the simulation continuously, responding to ping "
	echo "messages received from the sender".
	echo ""
	echo "The purpose of the test is to ensure that when joining a federation, the  "
	echo "group management features of the federate are maintained. This is not a   "
	echo "speed test, but rather a stability test to be run over time.              "
	echo ""
	echo " NOTE: Bundling can have an adverse effect on test performance. Turn it"
	echo "       off in the RTI.rid file if you'd like to speed things up a bit."
	echo ""
	echo "usage: lifecycle.sh [--arg <value>]"
	echo ""
	echo "    --federate-name      [stirng]    (REQUIRED) Name for this federate"
	echo "    --federation-name    [string]    (optional) Name of the federation we're joining, default hperf"
	echo "    --loops              [number]    (optional) Number of loops we should iterate for, default 20"
	echo "    --peers              [list]      (REQUIRED) Comma-separated list of other federate names"
	echo "    --packet-size        [number]    (optional) Min size of messages. e.g. 1B, 1K, 1M, default 1K"
	echo "    --sender                         (optional) Is this federate the one event sender, default false"
	echo "    --validate-data                  (optional) Validate received contents and log any errors, default false"
	echo "    --log-level          [string]    (optional) TRACE | DEBUG | INFO | WARN | FATAL | OFF, default INFO"
	echo ""
	echo "example: ./lifecycle-sh --federate-name one --peers two --loops 10000 --sender"
	echo ""
    
*/    
    
 // Driver pode ser { throughput-test, latency-test ou lifecycle-test }    
    
    @RequestMapping(value = "/start", method = RequestMethod.GET, produces=MediaType.APPLICATION_JSON_UTF8_VALUE )
	public @ResponseBody String start( 
			@RequestParam(value = "driver", required = true) String driver,
			@RequestParam(value = "peers", required = true) String peers
		) {
   		
    	federateService.start( driver, peers );
    	return "ok";
	}
        
	
}

