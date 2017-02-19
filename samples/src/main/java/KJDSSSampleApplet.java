/**
 * Project: KJ DSS
 * File   : KJDSSSampleApplet.java
 *
 * Author : Kristofer Fudalewski
 * Email  : sirk_sytes@hotmail.com   
 * Website: http://sirk.sytes.net
 */

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import com.fudcom.kjdss.KJDigitalSignalSynchronizer;
import com.fudcom.kjdss.ui.KJScopeAndSpectrumAnalyzer;

/**
 * @author Kris Fudalewski
 *
 * Sample application demonstrating KJDSS 
 */
public class KJDSSSampleApplet extends Applet implements Runnable {

	private static final int READ_BUFFER_SIZE = 1024 * 4;
	
	private Thread  mainThread;
	private boolean active = true;
	
	private KJDigitalSignalSynchronizer dss;
	
	public KJDSSSampleApplet() throws HeadlessException {
		super();
	}
    
	private void initDSS() {
		
		// -- Create a DSS.
		dss = new KJDigitalSignalSynchronizer();
		
		// -- Create DSP that comes with KJDSS (also used in KJ).
		KJScopeAndSpectrumAnalyzer wDsp = new KJScopeAndSpectrumAnalyzer();
		
		wDsp.setSpectrumAnalyserBandCount( 128 );
		wDsp.setSpectrumAnalyserBandDistribution( 
			new KJScopeAndSpectrumAnalyzer.LogBandDistribution( 4, 4.0 ) );
		
		wDsp.setBackground( Color.black );
		wDsp.setForeground( Color.gray );
		wDsp.setDisplayModeToggleOnMouseClickEnabled( false );
		wDsp.setDisplayMode( KJScopeAndSpectrumAnalyzer.DISPLAY_MODE_SPECTRUM_ANALYSER );
		
		// -- Add DSP to DSS.
		dss.add( wDsp );
		
		// -- Add DSP as component to JFrame
        add( (Component)wDsp );
		
	}

	public void init() {
		
		setLayout( new BorderLayout() );
		
		initDSS();
		
	}

	public void start() {
		
        setVisible( true );
        
		active = true;
		
        mainThread = new Thread( this );
        mainThread.start();
		
	}
	
	public void stop() {

//		System.out.println( "STOP!" );
		
		active = false;
		
		try {
			mainThread.join();
		} catch( InterruptedException pEx ) {
			// -- Do nothing.
		}
		
	}
	
	public void destroy() {

//		System.out.println( "DESTRROY!" );

		dss = null;
		
	}
	
	public void run() {
		
		mainLoop();
		
	}
	
	private void mainLoop() {

		File wLastFolder = new File( "" );
		
		while( active ) {
		
			// -- Choose a WAV file.
			File wAudioFile = chooseFile( wLastFolder );

			// -- Play the chosen WAV file, otherwise exit.
			if ( wAudioFile != null ) {
				
				wLastFolder = wAudioFile.getParentFile();
				
				playFile( wAudioFile );
				
			} else {
				active = false;
			}
			
		}
		
	}
	
	private void playFile( File pFile ) {
		
		try {
		
			String wNfn = pFile.getName().toLowerCase();
			
			if ( wNfn.endsWith( ".mp3" ) ) {
				playMP3File( pFile );
			} else if ( wNfn.endsWith( ".wav" ) ) {
				playWAVFile( pFile );
			} else {

				EventQueue.invokeAndWait(
					new Runnable() {
						public void run() {
							JOptionPane.showMessageDialog( 
							KJDSSSampleApplet.this, 
							"Unsupported file format!", 
							"Play Error!", 
							JOptionPane.ERROR_MESSAGE );
						}
					}
				);
				
			}
			
		} catch( final Exception pEx ) {
			
			pEx.printStackTrace();
			
			try {
			
				EventQueue.invokeAndWait(
					new Runnable() {
						public void run() {
							JOptionPane.showMessageDialog( 
								KJDSSSampleApplet.this, 
								"An error occurred during playback:\r\n" + pEx.getMessage(), 
								"Play Error!", 
								JOptionPane.ERROR_MESSAGE );
						}
					}
				);
				
			} catch( Exception pMwex ) {
				// -- Do nothing.
			}
			
		}
		
	}
	
	private void playMP3File( File pMp3File ) throws Exception {
		
		FileInputStream wFis = new FileInputStream( pMp3File );
		
		Bitstream wBitstream = new Bitstream( wFis );
	
		Header wHeader = wBitstream.readFrame();
		
		Decoder wDecoder = new Decoder(); 

		wDecoder.setOutputBuffer( 
		    new KJSampleBuffer( 
		        wHeader.frequency(), 
				wHeader.mode() == Header.SINGLE_CHANNEL ? 1 : 2 ) );

		KJSampleBuffer wSampleBuffer = (KJSampleBuffer)wDecoder.decodeFrame( wHeader, wBitstream );
		
        // -- Create a source data line in the format of the file.
    	SourceDataLine wSdl = AudioSystem.getSourceDataLine( 
    		new AudioFormat( 
    			wDecoder.getOutputFrequency(),
    			wHeader.mode() == Header.SINGLE_CHANNEL ? 8 : 16,
    			wDecoder.getOutputChannels(),
    			true, 
    			false ) );

    	// -- Open the source data line and start it.
    	wSdl.open();
    	wSdl.start();

    	// -- Have the DSS monitor the source data line.
        dss.start( wSdl );
    	
        // -- Decode MP3 data while active.
		while( active && wHeader != null ) {
		
			try {
			
				// -- Send to audio device.
				dss.writeAudioData( wSampleBuffer.getBuffer(), 0, wSampleBuffer.getBufferLength() );
				
                wBitstream.closeFrame();
                
	            // -- Read next frame.
	    		wHeader = wBitstream.readFrame();
	    		
	    		if ( wHeader != null ) {
	    			wSampleBuffer = (KJSampleBuffer)wDecoder.decodeFrame( wHeader, wBitstream );
	    		}
				
			} catch( Exception pEx ) {
				//System.err.println( pEx );
				pEx.printStackTrace();
			}

		}
		
    	// -- EOF, stop monitoring source data line.
    	dss.stop();
    	
    	// -- Stop and close the source data line.
    	wSdl.stop();
    	wSdl.close();
		
    	wFis.close();
        	
	}
	
	private void playWAVFile( File pWavFile ) throws Exception {
		
    	// -- Load the WAV file.
        AudioInputStream wAs = AudioSystem.getAudioInputStream( pWavFile );
        
        // -- Create a source data line in the format of the file.
    	SourceDataLine wSdl = AudioSystem.getSourceDataLine( wAs.getFormat() );

    	// -- Open the source data line and start it.
    	wSdl.open();
    	wSdl.start();

    	// -- Have the DSS monitor the source data line.
        dss.start( wSdl );
    	
        // -- Allocate a read buffer.
    	byte[] wRb = new byte[ READ_BUFFER_SIZE ];
    	int    wRs = 0;
    	
    	// -- Read from WAV file and write to DSS (and the monitored source data line)
    	while( active && ( wRs = wAs.read( wRb ) ) != -1 ) {
    		dss.writeAudioData( wRb, 0, wRs );
    	}

    	// -- EOF, stop monitoring source data line.
    	dss.stop();
    	
    	// -- Stop and close the source data line.
    	wSdl.stop();
    	wSdl.close();
            
	}
	
	
	private File chooseFile( File pLastFolder ) {
		
//		return new File( "B:\\kfud\\downloads\\Imogen Heap - Discography\\Releases\\Imogen Heap - Speak For Yourself (2005)\\Imogen Heap - Speak For Yourself - 05 - Hide And Seek.mp3" );
//		return new File( "B:\\kfud\\downloads\\Nederlandse Top 40 week 49 (2009) NLT-Release\\09 - David Guetta Ft. Akon - Sexy Bitch.mp3" );
		
		final JFileChooser wFc = new JFileChooser( pLastFolder );
		
		wFc.setDialogTitle( "KJDSS Sample Applet - Choose an audio file:" );
		
		wFc.setFileFilter(
				
			new FileFilter() {

				@Override
				public boolean accept( File pFile ) {
					
					if ( pFile.isDirectory() ) {
						return true;
					}
					
					String wNfn = pFile.getName().toLowerCase();
					
					return wNfn.endsWith( ".wav" ) || wNfn.endsWith( ".mp3" );
				}

				@Override
				public String getDescription() {
					return "*.mp3, *.wav";
				}
				
			}
			
		);
		
		wFc.setMultiSelectionEnabled( false );
		
		try {
		
			EventQueue.invokeAndWait(
				new Runnable() {
					public void run() {
						wFc.showOpenDialog( KJDSSSampleApplet.this );	
					}
				}
			);
			
		} catch( Exception pEx ) {
			// -- Do nothing.
		}
		
		return wFc.getSelectedFile();
		
	}
	
	public static void main( String[] pArgs ) {
		new KJDSSSampleApplet();
	}

}
