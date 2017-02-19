# KJDSS

The KJ Digital Signal Synchronizer (KJDSS) library allows developers of Java Sound based applications to implement real time digital signal processors like scopes and spectrum analysers. It includes an all Java scope and spectrum analyser component.

These package is open source and should be used at your own risk. 
Please read the license agreement. (GNU_LGP_License.htm)

This software package is available for download at: 

    http://www.fudcom.com/

If you have any questions, problems, or find any bugs, please email me at: 

    info@fudcom.com

----

## What is KJ Digital Signal Synchronizer (KJDSS)?

The KJ Digital Signal Synchronizer (renamed from KJ DSP Package) is a library for Java Sound that can be used to synchronize visual effects to speaker output.

## What is new in this version?

See the 'ReleaseNotes.txt' for details. 
 
## How do I use it?

You can look at the 'KJDSSSample.java' source file included in the 'kjdss130_src.zip' file OR consider this pseudo code example:

    KJDigitalSignalSynchronizer dss = new KJDigitalSignalSynchronizer();

    dss.start( sourceDataLine );

    while( decoder.hasMoreFrames() ) {

        byte[] wAudioDataFrame = decoder.decodeFrame();

        dss.write( wAudioDataFrame );

    }

- You must call the 'start' method on the DSS with the 'sourceDataLine' that will be sending output to the speakers. The DSS class will synchronize the output using the getLongFramePosition() of the sourceDataLine.
- Next you must write the audio data to the DSS which will also write the data to the source data line. The DSS will fire processing events to any KJDigitalSignalProcessor's registered with it.
- You must then register a KJDigitalSignalProcessor to the DSS audio data consumer that will be listening for processing events.
- Finally, you need process the audio data in some way to render it to the display.

## The KJ DSS Scope/Spectrum Analyzer Component

There is an AWT component called KJScopeAndSpectrumAnalyzer (com.fudcom.kjdss.ui) provided with this package that uses the KJ DSS.


 
