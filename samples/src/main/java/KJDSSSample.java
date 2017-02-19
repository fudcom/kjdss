/**
 * Project: KJ DSS
 * File   : KJDSSSample.java
 *
 * Author : Kristofer Fudalewski
 * Email  : sirk_sytes@hotmail.com   
 * Website: http://sirk.sytes.net
 */

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import com.fudcom.kjdss.KJDigitalSignalSynchronizer;
import com.fudcom.kjdss.ui.KJScopeAndSpectrumAnalyzer;

/**
 * @author Kris Fudalewski
 *
 * Sample application demonstrating KJDSS 
 */
public class KJDSSSample extends JFrame {

	private static final int READ_BUFFER_SIZE = 1024 * 4;
	
	private KJDigitalSignalSynchronizer dss;
	
	public KJDSSSample() throws HeadlessException {
		
		super();
		
		initGUI();
		
		initDSS();
		
        setVisible( true );
        
        mainLoop();

	}
    
	private void initDSS() {
		
		// -- Create a DSS.
		dss = new KJDigitalSignalSynchronizer();
		
		// -- Create DSP that comes with KJDSS (also used in KJ).
		KJScopeAndSpectrumAnalyzer wDsp = new KJScopeAndSpectrumAnalyzer();
		
		// -- Add DSP to DSS.
		dss.add( wDsp );
		
		// -- Add DSP as component to JFrame
        add( (Component)wDsp );
		
	}
	
	private void initGUI() {
		
		setTitle( "KJDSS - Sample" );

		setLayout( new BorderLayout() );
		
        setSize( 456, 208 );
        
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        
	}
	
	private void mainLoop() {
		
		while( true ) {
		
			// -- Choose a WAV file.
			File wWavFile = chooseFile();

			// -- Play the chosen WAV file, otherwise exit.
			if ( wWavFile != null ) {
				playFile( wWavFile );
			} else {
				break;
			}
			
		}
		
		System.exit( 0 );
		
	}
	
	private void playFile( File pWavFile ) {
		
        try {
	        
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
        	while( ( wRs = wAs.read( wRb ) ) != -1 ) {
        		dss.writeAudioData( wRb, 0, wRs );
        	}

        	// -- EOF, stop monitoring source data line.
        	dss.stop();
        	
        	// -- Stop and close the source data line.
        	wSdl.stop();
        	wSdl.close();
            
        } catch( Exception pEx ) {
        	pEx.printStackTrace();
        }
		
	}
	
	
	private File chooseFile() {
		
		JFileChooser wFc = new JFileChooser();
		
		wFc.setFileFilter( 
			new FileFilter() {

				@Override
				public boolean accept( File pFile ) {
					return pFile.getName().toLowerCase().endsWith( ".wav" ) || pFile.isDirectory();
				}

				@Override
				public String getDescription() {
					return "*.wav";
				}
				
			}
		);
		
		wFc.setMultiSelectionEnabled( false );
		
		wFc.showOpenDialog( this );
		
		return wFc.getSelectedFile();
		
	}
	
	
	public static void main( String[] pArgs ) {
		new KJDSSSample();
	}

}
