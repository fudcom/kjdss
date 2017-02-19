

import javazoom.jl.decoder.Obuffer;

/**
 * @author Kris
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class KJSampleBuffer extends Obuffer {

	  private byte[] buffer;
	  private int[]  bufferp;

	  private int    channels;
	  private int	 frequency;
	  
//	  float[] fft_real;
//	  double fft_imaginary[];
	  
//	  int    fft_pos;
						   
	  public KJSampleBuffer( int pSampleFrequency, int pNumberOfChannels ) {

          buffer = new byte[ OBUFFERSIZE * pNumberOfChannels ];
		  bufferp = new int[ MAXCHANNELS ];
	  	
	//	  fft_real = new float[ OBUFFERSIZE ];
	//	  fft_imaginary = new double[ OBUFFERSIZE ];
		  
		  channels = pNumberOfChannels;
		  frequency = pSampleFrequency;
		
	  }

	  public int getChannelCount() {
		return channels;  
	  }
	  
	  public int getSampleFrequency() {
		  return frequency;
	  }
	  
	  public byte[] getBuffer() {
	       return this.buffer;  
	  }
	  
	  public int getBufferLength() {
		  return bufferp[ 0 ];
	  }
	  
//	  public float[] getAudioData() {
//	  	return fft_real;
//	  }
//	  
//	  public float[] getFFTForSample() {
//	  	
//	      return KJFFT.fftMag( fft_real, 1024 );
//	  	
////	      return fft_real;
//	      
//	  }
	  
	  /**
	   * Takes a 16 Bit PCM sample.
	   */
	  public void append( int pChannel, short pValue ) {
	  	
 		  buffer[ bufferp[ pChannel ] ]     = (byte)pValue;
 		  buffer[ bufferp[ pChannel ] + 1 ] = (byte)( pValue >>> 8 );
 		  
		  bufferp[ pChannel ] += channels << 1;	  	
	      
	  }
	  
		public void appendSamples( int pChannel, float[] pSamples ) {
			
		    int wPosition = bufferp[ pChannel ];
			
		    for ( int i = 0; i < 32; i++ ) {
		    	
		    	float wFloatSample = pSamples[ i ];
		    	
//		    	if ( pChannel == 0 ) {
//		    		fft_real[ fft_pos + i ] = pSamples[ i ] / 32767.0f;
//		    	}
		    	
	     //       fft_imaginary[ fft_pos + i ] = 0.0;					  
		    	
				wFloatSample = ( wFloatSample > 32767.0f ? 32767.0f : ( wFloatSample < -32767.0f ? -32767.0f : wFloatSample ) );
				
				short wSample = (short)wFloatSample;
				
				buffer[ wPosition ]     = (byte)wSample;
				buffer[ wPosition + 1 ] = (byte)( wSample >>> 8 );
				
				wPosition += ( channels << 1 );
				
		    }
			
			bufferp[ pChannel ] = wPosition;
			
//			if ( pChannel == 0 ) {
//				fft_pos += 32;
//			}
			
		}
	  
	  
	  /**
	   * Write the samples to the file (Random Acces).
	   */
	  public void write_buffer(int val)
	  {
					  
		//for (int i = 0; i < channels; ++i) 
		//	bufferp[i] = (short)i;

	  }

	  public void close()
	  {}
	  
	  /**
	   *
	   */
	  public void clear_buffer() {
	  	
          for( int i = 0; i < channels; i++ ) {
              bufferp[ i ] = i << 1;
          }
          
      //   fft_pos = 0;
          
	  }
	  
	  /**
	   *
	   */
	  public void set_stop_flag()
	  {}

}


