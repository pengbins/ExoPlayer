/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.media3.transformer;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.util.Size;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for creating and configuring a {@link FrameProcessorChain}.
 *
 * <p>See {@link FrameProcessorChainPixelTest} for data processing tests.
 */
@RunWith(AndroidJUnit4.class)
public final class FrameProcessorChainTest {
  private final AtomicReference<FrameProcessingException> frameProcessingException =
      new AtomicReference<>();

  @Test
  public void getOutputSize_noOperation_returnsInputSize() throws Exception {
    Size inputSize = new Size(200, 100);
    FrameProcessorChain frameProcessorChain =
        createFrameProcessorChainWithFakeFrameProcessors(
            /* pixelWidthHeightRatio= */ 1f,
            inputSize,
            /* frameProcessorOutputSizes= */ ImmutableList.of());

    Size outputSize = frameProcessorChain.getOutputSize();

    assertThat(outputSize).isEqualTo(inputSize);
    assertThat(frameProcessingException.get()).isNull();
  }

  @Test
  public void getOutputSize_withWidePixels_returnsWiderOutputSize() throws Exception {
    Size inputSize = new Size(200, 100);
    FrameProcessorChain frameProcessorChain =
        createFrameProcessorChainWithFakeFrameProcessors(
            /* pixelWidthHeightRatio= */ 2f,
            inputSize,
            /* frameProcessorOutputSizes= */ ImmutableList.of());

    Size outputSize = frameProcessorChain.getOutputSize();

    assertThat(outputSize).isEqualTo(new Size(400, 100));
    assertThat(frameProcessingException.get()).isNull();
  }

  @Test
  public void getOutputSize_withTallPixels_returnsTallerOutputSize() throws Exception {
    Size inputSize = new Size(200, 100);
    FrameProcessorChain frameProcessorChain =
        createFrameProcessorChainWithFakeFrameProcessors(
            /* pixelWidthHeightRatio= */ .5f,
            inputSize,
            /* frameProcessorOutputSizes= */ ImmutableList.of());

    Size outputSize = frameProcessorChain.getOutputSize();

    assertThat(outputSize).isEqualTo(new Size(200, 200));
    assertThat(frameProcessingException.get()).isNull();
  }

  @Test
  public void getOutputSize_withOneFrameProcessor_returnsItsOutputSize() throws Exception {
    Size inputSize = new Size(200, 100);
    Size frameProcessorOutputSize = new Size(300, 250);
    FrameProcessorChain frameProcessorChain =
        createFrameProcessorChainWithFakeFrameProcessors(
            /* pixelWidthHeightRatio= */ 1f,
            inputSize,
            /* frameProcessorOutputSizes= */ ImmutableList.of(frameProcessorOutputSize));

    Size frameProcessorChainOutputSize = frameProcessorChain.getOutputSize();

    assertThat(frameProcessorChainOutputSize).isEqualTo(frameProcessorOutputSize);
    assertThat(frameProcessingException.get()).isNull();
  }

  @Test
  public void getOutputSize_withThreeFrameProcessors_returnsLastOutputSize() throws Exception {
    Size inputSize = new Size(200, 100);
    Size outputSize1 = new Size(300, 250);
    Size outputSize2 = new Size(400, 244);
    Size outputSize3 = new Size(150, 160);
    FrameProcessorChain frameProcessorChain =
        createFrameProcessorChainWithFakeFrameProcessors(
            /* pixelWidthHeightRatio= */ 1f,
            inputSize,
            /* frameProcessorOutputSizes= */ ImmutableList.of(
                outputSize1, outputSize2, outputSize3));

    Size frameProcessorChainOutputSize = frameProcessorChain.getOutputSize();

    assertThat(frameProcessorChainOutputSize).isEqualTo(outputSize3);
    assertThat(frameProcessingException.get()).isNull();
  }

  private FrameProcessorChain createFrameProcessorChainWithFakeFrameProcessors(
      float pixelWidthHeightRatio, Size inputSize, List<Size> frameProcessorOutputSizes)
      throws FrameProcessingException {
    ImmutableList.Builder<GlEffect> effects = new ImmutableList.Builder<>();
    for (Size element : frameProcessorOutputSizes) {
      effects.add(() -> new FakeFrameProcessor(element));
    }
    return FrameProcessorChain.create(
        getApplicationContext(),
        /* listener= */ this.frameProcessingException::set,
        pixelWidthHeightRatio,
        inputSize.getWidth(),
        inputSize.getHeight(),
        effects.build(),
        /* enableExperimentalHdrEditing= */ false);
  }

  private static class FakeFrameProcessor implements GlFrameProcessor {

    private final Size outputSize;

    private FakeFrameProcessor(Size outputSize) {
      this.outputSize = outputSize;
    }

    @Override
    public void initialize(Context context, int inputTexId, int inputWidth, int inputHeight) {}

    @Override
    public Size getOutputSize() {
      return outputSize;
    }

    @Override
    public void drawFrame(long presentationTimeNs) {}

    @Override
    public void release() {}
  }
}
