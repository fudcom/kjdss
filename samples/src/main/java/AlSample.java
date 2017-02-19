

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import com.fudcom.kjdss.KJDigitalSignalSynchronizer;
import com.fudcom.kjdss.ui.KJScopeAndSpectrumAnalyzer;

public class AlSample extends JFrame {

	// This string needs to be set to the first few characters needed to uniquely identify
	// the Mixer (by name) on which a TargetDataLine is to be opened for reading audio data.
	// Typical values: "Stereo", "CD", "Line"
	private final static String mixerNameStartString = "Mic";
																 
	private static final int READ_BUFFER_SIZE = 1024;
	
	private KJDigitalSignalSynchronizer dss;
	
	public AlSample() throws HeadlessException {
		
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
		
//		while( true ) {
		
			// -- Choose a WAV file.
//			File wWavFile = chooseFile();
		
			// Instead of opening a file to read, get a TargetDataLine as an audio source
			// In this context it is a source of audio data
			TargetDataLine srcLine = null;
			// Same AudioFormat as used by .WAV files
			AudioFormat fmt = new AudioFormat(44100F,16,2,true,false);
			Line.Info reqLineInfo = new DataLine.Info(TargetDataLine.class,fmt);
			if (!AudioSystem.isLineSupported(reqLineInfo)) {
				System.out.println("TDL not supported");
				System.exit(-1);
			}
			Mixer.Info[] mixInfoAr = AudioSystem.getMixerInfo();
			System.out.println("Found " + mixInfoAr.length + " Mixers. Looking for Mixer \"" +
								mixerNameStartString + "...\"");
			for ( Mixer.Info mixInfo : mixInfoAr ) {
				Mixer mix = AudioSystem.getMixer(mixInfo);
				
				System.out.println( "Mixer: " + mixInfo.getName() );
				
				if (mix.isLineSupported(reqLineInfo)) {
					try {

						if (mixInfo.getName().startsWith(mixerNameStartString)) {
							System.out.println("Found Mixer \"" + mixInfo.getName() + "\"");
							srcLine = (TargetDataLine)(mix.getLine(reqLineInfo));
							System.out.println("Got TargetDataLine");
							srcLine.open(fmt);
							System.out.println("Opened TargetDataLine");
							srcLine.start();
							break;
						}
					} catch (Exception ex) {
						System.out.println("Can't get TargetDataLine on Mixer: " + ex.getMessage());
					}
				}
			}

			// This will never return...
			playStream(srcLine);			

			// -- Play the chosen WAV file, otherwise exit.
/*			if ( wWavFile != null ) {
				playFile( wWavFile );
			} else {
				break;
			}
*/			
//		}
		
		System.exit( 0 );
		
	}
	
	// was: private void playFile( File pWavFile ) {
	private void playStream( TargetDataLine tdl) {
		
        try {
        	
        	// No need for that AudioInputStream - can read from the TargetDataLine directly	        
        	// -- Load the WAV file.
//            AudioInputStream wAs = AudioSystem.getAudioInputStream( pWavFile );

            // -- Create a source data line in the same format as the TargetDataLine
 //       	SourceDataLine wSdl = AudioSystem.getSourceDataLine( tdl.getFormat() ); //wAs.getFormat() );

        	// -- Open the source data line and start it.
//        	wSdl.open();
//        	wSdl.start();

        	// Disable writing to the SourceDataLine, since some other appliation will already
        	// be playing the audio we wish to visualize
        	dss.setSourceDataLineWriteEnabled( false );
        	
        	// -- Have the DSS monitor the target data line.
            dss.start( tdl );
            
            // -- Allocate a read buffer.
        	byte[] wRb = new byte[ 1024 ];
        	int    wRs = 0;

        	// -- Read from WAV file and write to DSS (and the monitored source data line)
        	//while( ( wRs = wAs.read( wRb ) ) != -1 ) {
        	while( ( wRs = tdl.read( wRb, 0, wRb.length ) ) != -1 ) {
//        		
//		//		System.out.println( tdl.getLongFramePosition() );
//
        		dss.writeAudioData( wRb, 0, wRs );
        		
        	}

        	// The while loop above will never terminate
        	
        	// -- EOF, stop monitoring source data line.
        	dss.stop();
        	
        	// -- Stop and close the source data line.
//        	wSdl.stop();
//        	wSdl.close();
            
        } catch( Exception pEx ) {
        	pEx.printStackTrace();
        }
		
	}
	
	// Not used
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
		new AlSample();
	}

	public class Normalizer {

//		private float[] left;
//		private float[] right;

		private AudioFormat audioFormat;
		
		private float[][] channels;

		private int  sampleSize;
		private int  channelSize;
		private long audioSampleSize;
		
		public Normalizer( AudioFormat pFormat ) {

			audioFormat = pFormat;
			
			sampleSize = (int)( Math.round( audioFormat.getFrameRate() ) );
			
			channels = new float[ pFormat.getChannels() ][]; 
			
			for( int c = 0; c < pFormat.getChannels(); c++ ) {
				channels[ c ] = new float[ sampleSize ];
			}
			
			channelSize     = audioFormat.getFrameSize() / audioFormat.getChannels();
			audioSampleSize = ( 1 << ( audioFormat.getSampleSizeInBits() - 1 ) );
			
			//audioFormat.getEncoding()
			
		}
		
		public float[][] normalize( byte[] pData, int pPosition, int pLength ) {
			
			int wChannels  = audioFormat.getChannels();
			int wSsib      = audioFormat.getSampleSizeInBits();
			int wFrameSize = audioFormat.getFrameSize();
			
			// -- Loop through audio data.
			for( int sp = 0; sp < sampleSize; sp++ ) { 
				
				if ( pPosition >= pData.length ) {
					pPosition = 0;
				}
				
				int cdp = 0;
				
				// -- Loop through channels.
				for( int ch = 0; ch < wChannels; ch++ ) {

					// -- Sign least significant byte. (PCM_SIGNED)
					long sm = ( pData[ pPosition + cdp ] & 0xFF ) - 128;
//					long sm = ( pData[ pPosition + cdp ] ); // & 0xFF ) - 128;
					
					for( int bt = 8, bp = 1; bt < wSsib; bt += 8 ) {
						sm += pData[ pPosition + cdp + bp ] << bt;
						bp++;
					}
					
					// -- Store normalized data.
					channels[ ch ][ sp ] = (float)sm / audioSampleSize;
				
//					System.out.println( "SP[" + sp + "] " + channels[ ch ][ sp ] );
					
					cdp += channelSize;
					
				}
					
				pPosition += wFrameSize;
				
			}
			
//			System.out.println( "------------------------" );

			//	System.out.println( "FS: " + audioFormat.getFrameSize() );
			
//			if ( wCc == 1 ) {
//			
//				if ( wSs == 8 ) {
//					
//					for( int a = 0, c = pPosition; a < sampleSize; a++, c += wFs ) { 
//					
//						if ( c >= pData.length ) {
//							c = 0;
//						}
//						
//						channels[ 0 ][ a ] = (float)( ( ( pData[ c ] & 0xFF ) - 128 ) / 128.0f );
//						
////						System.out.print( channels[ 0 ][ a ]  + " " );
//						//right[ a ] = left[ a ];
//						
//					}
//					
//				} else if ( wSs == 16 ) {
//					
//					for( int a = 0, c = pPosition; a < sampleSize; a++, c += wFs ) { 
//						
//						if ( c >= pData.length ) {
//							c = 0;
//						}
//						
//						channels[ 0 ][ a ]  = (float)( ( (int)pData[ c + 1 ] << 8 ) + pData[ c ] ) / 32768.0f;
//						//right[ a ] = left[ a ];
//						
//	//					System.out.print( channels[ 0 ][ a ] + " " );
//						
//					}
//					
//				}
//				
//			} else if ( wCc == 2 ) {
//			
//				if ( wSs == 8 ) {
//				
//					for( int a = 0, c = pPosition; a < sampleSize; a++, c += wFs ) { 
//						
//						if ( c >= pData.length ) {
//							c = 0;
//						}
//						
//						channels[ 0 ][ a ] = (float)( ( ( pData[ c ] & 0xFF ) - 128 ) / 128.0f );
//						channels[ 1 ][ a ] = (float)( ( ( pData[ c + 1 ] & 0xFF ) - 128 ) / 128.0f );
//						
//					}
//					
//				} else if ( wSs == 16 ) {
//				
//					for( int a = 0, c = pPosition; a < sampleSize; a++, c += wFs ) { 
//						
//						if ( c >= pData.length ) {
//							c = 0;
//						}
//						
//						channels[ 0 ][ a ] = (float)( ( (int)pData[ c + 1 ] << 8 ) + pData[ c ] ) / 32768.0f;
//						channels[ 1 ][ a ] = (float)( ( (int)pData[ c + 3 ] << 8 ) + pData[ c + 2 ] ) / 32768.0f;
//						
//					}
//					
//				}
//				
//			}
			
			return channels;
			
		}
		
	}
	
}
