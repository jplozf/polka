package fr.ligorax.polka;

import android.app.*;
import android.media.*;
import android.os.*;

public class PlaySound
{
	// originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
	// and modified by Steve Pomeroy <steve@staticfree.info>
	// and modified also by Jean-Pierre Liguori <ligorax@free.fr> 
	private int duration; // = 3000; // milliseconds
	private double freqOfTone; // = 440; // hz
	private int sampleRate = 8000;
	private int numSamples; // = duration * sampleRate;
	private double sample[]; // = new double[numSamples];
	private byte generatedSnd[]; // = new byte[2 * numSamples];

	Handler handler = new Handler();

	public PlaySound(int duration, double freqOfTone, boolean sync)
	{
		// Use a new thread as this can take a while
		this.duration = duration;
		this.freqOfTone = freqOfTone;
		this.numSamples = this.duration * this.sampleRate;
		this.sample = new double[this.numSamples];
		this.generatedSnd = new byte[2 * this.numSamples];

		if (sync == false)
		{
			final Thread thread = new Thread(new Runnable() {
					public void run()
					{
						genTone();
						handler.post(new Runnable() {

								public void run()
								{
									playSound();
								}
							});
					}
				});
			thread.start();
		}
		else
		{
			genTone();
			playSoundAsynchronous();
		}
	}

	void genTone()
	{
		// fill out the array
		for (int i = 0; i < this.numSamples; ++i)
		{
			this.sample[i] = Math.sin(2 * Math.PI * i / (this.sampleRate / this.freqOfTone));
		}

		// convert to 16 bit pcm sound array
		// assumes the sample buffer is normalised.
		int idx = 0;
		for (final double dVal : this.sample)
		{
			// scale to maximum amplitude
			final short val = (short) ((dVal * 32767));
			// in 16 bit wav PCM, first byte is the low order byte
			this.generatedSnd[idx++] = (byte) (val & 0x00ff);
			this.generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

		}
	}

	void playSound()
	{
		final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
													 sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
													 AudioFormat.ENCODING_PCM_16BIT, numSamples,
													 AudioTrack.MODE_STATIC);
		audioTrack.write(this.generatedSnd, 0, this.generatedSnd.length);
		audioTrack.play();
	}

	void playSoundAsynchronous()
	{
		final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
													 sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
													 AudioFormat.ENCODING_PCM_16BIT, numSamples,
													 AudioTrack.MODE_STATIC);
		audioTrack.write(this.generatedSnd, 0, this.generatedSnd.length);
		audioTrack.play();
		int x;
		do
		{
			x = audioTrack.getPlaybackHeadPosition();
		} while (x < this.numSamples / 2);
	}
}
