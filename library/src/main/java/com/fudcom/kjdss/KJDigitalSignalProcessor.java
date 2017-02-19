/**
 * Project: KJ DSS
 * File   : KJDigitalSignalProcessor.java
 *
 * Author : Kristofer Fudalewski
 * Email  : info@fudcom.com   
 * Website: http://www.fudcom.com
 */
package com.fudcom.kjdss;

import javax.sound.sampled.AudioFormat;


/**
 * @author Kris Fudalewski
 * 
 * Classes must implement this interface in order to be registered with the 
 * KJDigitalSignalSynchronizer class.
 *  
 */
public interface KJDigitalSignalProcessor {

	/**
	 * Called by the KJDigitalSignalSynchronizer during the call of the 'start' method.
	 * Allows a DSP to prepare any necessary buffers or objects according to the audio format of
	 * the source data line.
	 * 
	 * @param pSampleSize     The sample size that this DSP should be prepared to handle.
	 * @param pAudioFormat    The audio format of passed in through the start method.
	 */
	void initialize( int pSampleSize, AudioFormat pAudioFormat );
	
	/**
	 * Called by the KJDigitalSignalSynchronizer while the SourceDataLine is active.
	 * 
	 * @param pDssContext A context object containing a reference to the sample data to be processed
	 * 					  as well as other useful references during processing time.
	 */
    void process( KJDigitalSignalSynchronizer.Context pDssContext );
	
}
