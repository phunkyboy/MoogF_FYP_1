package com.example.moog_project_2;

import static com.example.moog_project_2.ClickKnob.waveForm;
       // import android.content.Intent;
      //  import android.os.Bundle;
       // import android.support.design.widget.FloatingActionButton;
       // import android.support.design.widget.Snackbar;
        import android.support.v7.app.AppCompatActivity;
       // import android.support.v7.widget.Toolbar;
       // import android.widget.Toast;
//  import android.view.MotionEvent;
      // import android.view.View;
      //  import android.view.Menu;
import android.media.AudioFormat;
        import android.media.AudioManager;
        import android.media.AudioTrack;
     //  import android.graphics.Typeface;
    //   import android.widget.TextView;
   //    import android.widget.ImageButton;
  //     import android.widget.Toast;
        import static com.example.moog_project_2.ADSR.SR;
        import static com.example.moog_project_2.MainActivity.freq;
        import static com.example.moog_project_2.MainActivity.FilterCutoff;
        import static com.example.moog_project_2.MainActivity.amp;







public class KnobSynthesis extends AppCompatActivity {


    //double mix=0.5; //this is a value from 0 to 1 to balance between Osc1 and Osc 2
    double Amp = 20; //this should be between 0 and 100%

    //----------------------ENVELOPE PARAMETERS---------------------------------------------------
    double AmpEnvAttack = 1.25; //in seconds
    double AmpEnvDecay = 2.5; //in seconds
    double AmpEnvSustainTime = 4; //in seconds
    double AmpEnvSustainLevel = 0.6;
    double AmpEnvRelease = 5; //in seconds

//---------------------SALLEN KEY VARIABLES--------------------------------------------

    //double FilterCutoff = 500; //this is in hertz
    double FilterEnvDepth = 500; // in hertz - adjusts cutoff
    double FilterEnv;
    //double FilterInput;

    double Res = 1;
    double FilterEnvAttack = 2; //in seconds
    double FilterEnvDecay = 3.2; //in seconds
    double FilterEnvSustainTime = 10;//in seconds
    double FilterEnvSustainLevel = 0.4;
    double FilterEnvRelease = 5; //in seconds

//----------------------START OF THREAD----------------------------------------------------------


    Thread t;
    int samplerate = 44100;
    int amp = 400;
    double twoPi = 8. * Math.atan(1.);
    double freq = 1000.f;
    double ph;
    int bufferSize = AudioTrack.getMinBufferSize(samplerate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
    int ampEnv = 0;

    //int bufferSize = 1024;

    short samples[] = new short[bufferSize];
    //short samples_play[] = new short[bufferSize];
    double AmpEnv;
    static boolean isRunning;
    double samp = 0.0;


    //------------------------------PLAYBACK SETUP----------------------------------------------

    //CREATE  THREAD SECTION
    public void playNote() {
        isRunning = true;

        //waveForm=ClickKnob.getWaveForm();

        t = new Thread() {
            public void run() {


                setPriority(Thread.MAX_PRIORITY);

                AudioTrack trackOne = new AudioTrack(AudioManager.STREAM_MUSIC, samplerate,
                        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);


                trackOne.play(); // prepare to play

                //-----------------------------------END OF PLAY SECTION--------------------------------------

                while (isRunning) { //WHILE THREAD IS RUNNING.


                    ///---------------switch statement for waveform selection------------------





                    switch (waveForm) {

                        case "sine":

                            samples = sine(samples, bufferSize, freq, amp);

                            break;

                        case "saw":

                            samples = saw(samples, bufferSize, freq);

                            break;

                        case "triangle":

                           samples = triangle(samples, bufferSize, freq);

                            break;

                        case "square":

                           samples = square(samples, bufferSize, freq);

                            break;

                        default:

                            throw new IllegalArgumentException("invalid string");

                    }


                    samples = sine(samples, bufferSize, freq, amp);


                 // samples = saw(samples, bufferSize, freq);


                   // samples = triangle(samples, bufferSize, freq);


                   // samples = square(samples, bufferSize, freq);






//---------------------------------------PLAY AUDIO OUTPUT-----------------------------------


                    trackOne.write(samples, 0, bufferSize);   //write the e samples array to trackOne


                }

            }

        };


        t.start();

    }


//----------------------end of thread---------------------------------------------------------------


//*******************WAVEFORM SYNTHESIS ALGORITHM SECTION*******************************************


//-----------------------SINE WAVE WITH SOUND ENGINE---------------------------------------

    public short[] sine(short[] samples, int bufferSize, double freq ,int amp) {


        double F_in, sf; // F_in is the filter input from osc1
        double output;

        ADSR adsrFilt = new ADSR();
        ADSR adsrAmp = new ADSR();
        Filter filt = new Filter();

        double FilterInput;
        double FilterOutput;


        for (int sampleIndex = 0; sampleIndex < bufferSize; sampleIndex++) {


            F_in = (amp * Math.cos(ph));

            FilterInput = F_in;


           // AmpEnv = adsrAmp.envGenNew(ampEnv, 4, 1.0, 50, 10, 50, SR);

            //FilterEnv = adsrFilt.envGenNew(sampleIndex, 1.5, 3.0, 50, .5, 50, SR);  // line with hardcoded values see above for the method case

          //  FilterEnv = FilterCutoff + (FilterEnvDepth - FilterCutoff) * FilterEnv;

          //  FilterOutput = Filter.SallenKeyfiltering(FilterInput, FilterEnv, 0.5f, SR);
          //  ampEnv++; //

//--------------------------------------------------------------------------------------------------------
            //output = FilterOutput;
            //output = FilterEnv;
            // output = AmpEnv* (FilterInput *FilterOutput); // Amp is used to scale input between 1 and -1

//------------------------------------------------------------------------------------------------------------
           output = FilterInput;


           // output = (Amp / 100) * AmpEnv * FilterOutput;


            samples[sampleIndex] = (short) (output);
            ph += twoPi * freq / samplerate;

        }


        return samples;

    }

//--------------------------------SAW WAVE WITH SOUND ENGINE-----------------------------------------------------------------------------

    public short[] saw(short[] samples, int bufferSize, double freq){
        samp = 0.0;
        int k;
        int K=(int) Math.floor(samplerate*0.5/freq);


        ADSR adsrFilt = new ADSR();
        ADSR adsrAmp = new ADSR();
        Filter filt = new Filter();



        for (int sampleIndex = 0; sampleIndex < bufferSize; sampleIndex++){
            double FilterInput;
            double FilterOutput;
            double F_in, sf;
            double output;

            double s, sw = 0.0;
            for(k=1; k<K; k++) {
                samp = Math.pow((double) k, -1.0);
                samp=samp/(twoPi/2.0);
                s = samp * Math.sin((double) k * ph);
                sw = sw+s;
            }
           // samples[sampleIndex] = (short) ((amp*0.5)*sw);

            F_in= (short) ((amp*0.5)*sw);

            FilterInput = F_in;


            AmpEnv = adsrAmp.envGenNew(ampEnv, 4, 1.0, 50, 10, 50, SR);

            FilterEnv = adsrFilt.envGenNew(sampleIndex, 1.5, 3.0, 50, .5, 50, SR);  // line with hardcoded values see above for the method case

            FilterEnv = FilterCutoff + (FilterEnvDepth - FilterCutoff) * FilterEnv;

            FilterOutput = Filter.SallenKeyfiltering(FilterInput, FilterEnv, 0.5f, SR);
            ampEnv++; //

            output = (Amp / 100) * AmpEnv * FilterOutput;




            output = FilterInput; //todo remove after test



            //output = FilterOutput;
            samples[sampleIndex] = (short) (output);

            ph += twoPi * freq / samplerate;

//            samples[i] = (short) (amp*2.0*((i*freq/samplerate)%1)-1);
            //          samples[i]=(short)(amp*2*((((i*freq)/samplerate) %1))-1);
        }
        return samples;
    }










//------------------------SQUARE wave with sound engine------------------------------------------


    public short[] square(short[] samples, int bufferSize, double freq){
        samp = 0.0;
        int k;
        int K=(int) Math.floor(samplerate*0.5/freq);


        ADSR adsrFilt = new ADSR();
        ADSR adsrAmp = new ADSR();
        Filter filt = new Filter();


        for (int sampleIndex = 0; sampleIndex < bufferSize; sampleIndex++){
            double FilterInput;
            double FilterOutput;
            double F_in, sf;
            double output;

            double s, sw = 0.0;
            for(k=1; k<K; k+=2) {
                samp = Math.pow((double) k, -1.0);
                s = samp * Math.sin((double) k * ph);
                sw = sw+s;
            }

            F_in= (short) ((amp*0.5)*sw);

            FilterInput = F_in;


            AmpEnv = adsrAmp.envGenNew(ampEnv, 4, 1.0, 50, 10, 50, SR);

            FilterEnv = adsrFilt.envGenNew(sampleIndex, 1.5, 3.0, 50, .5, 50, SR);  // line with hardcoded values see above for the method case

            FilterEnv = FilterCutoff + (FilterEnvDepth - FilterCutoff) * FilterEnv;

            FilterOutput = Filter.SallenKeyfiltering(FilterInput, FilterEnv, 0.5f, SR);
            ampEnv++; //

            output = (Amp / 100) * AmpEnv * FilterOutput;

            samples[sampleIndex] = (short) (output);

            ph += twoPi * freq / samplerate;

        }
        return samples;
    }

//----------------------TRIANGLE wave with sound engine -----------------------------------------

    public short[] triangle(short[] samples, int bufferSize, double freq){
        samp = 0.0;
        int k;
        int K=(int) Math.floor(samplerate*0.5/freq);


        ADSR adsrFilt = new ADSR();
        ADSR adsrAmp = new ADSR();
        Filter filt = new Filter();


        for (int sampleIndex = 0; sampleIndex < bufferSize; sampleIndex++){
            double FilterInput;
            double FilterOutput;
            double F_in, sf;
            double output;
            double s, sw = 0.0;

            for(k=1; k<K; k++) {
                samp = Math.pow((double) k, -1.0);
                samp=samp/(twoPi/2.0);
                s = samp * Math.sin((double) k * ph);

                sw=sw+s;
            }

            F_in = (short) (amp*0.5*sw);

            FilterInput = F_in;

            AmpEnv = adsrAmp.envGenNew(ampEnv, 4, 1.0, 50, 10, 50, SR);

            FilterEnv = adsrFilt.envGenNew(sampleIndex, 1.5, 3.0, 50, .5, 50, SR);  // line with hardcoded values see above for the method case

            FilterEnv = FilterCutoff + (FilterEnvDepth - FilterCutoff) * FilterEnv;

            FilterOutput = Filter.SallenKeyfiltering(FilterInput, FilterEnv, 0.5f, SR);
            ampEnv++; //


            output = (Amp / 100) * AmpEnv * FilterOutput;


            samples[sampleIndex] = (short) (output);
            ph += twoPi * freq / samplerate;
        }
        return samples;
    }




//----------------------------------END OF WAVEFORM SYMTHESIS-------------------------------------




























//----------------------------------TEST (JUNK) CODE -------------------------------------------------------


    // output = F_in;  //todo code to test output from oscillator and input to filter

    // AmpEnv = adsrAmp.envGenNew(FilterOutput,AmpEnvAttack, AmpEnvDecay,AmpEnvSustainTime,AmpEnvSustainLevel,  AmpEnvRelease, SR);

    //todo added line 236 and various hardcoded values.


    // AmpEnv = adsrAmp.envGenNew(ampEnv, 1, 1.0, 50,1,50,SR); todo test values only...


    //FilterEnv = adsrFilt.envGenNew(sampleIndex,FilterEnvAttack, FilterEnvDecay, FilterEnvSustainTime,FilterEnvSustainLevel,  FilterEnvRelease, SR);





/*            AmpEnv = adsrAmp.envGenNew(ampEnv, 4, 1.0, 50, 10, 50, SR);

            FilterEnv = adsrFilt.envGenNew(sampleIndex, 1.5, 3.0, 50, .5, 50, SR);  // line with hardcoded values see above for the method case

            FilterEnv = FilterCutoff + (FilterEnvDepth - FilterCutoff) * FilterEnv;

            FilterOutput = Filter.SallenKeyfiltering(FilterInput, FilterEnv, 0.5f, SR);
            ampEnv++; //

//--------------------------------------------------------------------------------------------------------
            //output = FilterOutput;
            //output = FilterEnv;
            // output = AmpEnv* (FilterInput *FilterOutput); // Amp is used to scale input between 1 and -1

//------------------------------------------------------------------------------------------------------------
            output = (Amp / 100) * AmpEnv * FilterOutput;


            samples[sampleIndex] = (short) (output);
            ph += twoPi * freq / samplerate;

        }


        return samples;


    }




//----------------------------------TEST CODE -------------------------------------------------------


    // output = F_in;  //todo code to test output from oscillator and input to filter

    // AmpEnv = adsrAmp.envGenNew(FilterOutput,AmpEnvAttack, AmpEnvDecay,AmpEnvSustainTime,AmpEnvSustainLevel,  AmpEnvRelease, SR);

    //todo added line 236 and various hardcoded values.


    // AmpEnv = adsrAmp.envGenNew(ampEnv, 1, 1.0, 50,1,50,SR); todo test values only...


    //FilterEnv = adsrFilt.envGenNew(sampleIndex,FilterEnvAttack, FilterEnvDecay, FilterEnvSustainTime,FilterEnvSustainLevel,  FilterEnvRelease, SR);

//-----------------------------------------------------------------------------------------------------------

*/





























}








