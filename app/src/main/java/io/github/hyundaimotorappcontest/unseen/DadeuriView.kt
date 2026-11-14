package io.github.hyundaimotorappcontest.unseen

import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/**
 * 다드리: the voice agent's glass prism visual that eases between voice states.
 *
 * AGSL port of prism-orb 1.0.0 with its Sky palette, simplex field and inflow, churn and bloom
 * motions. Below API 33, or if the shader is unavailable, it draws a static gradient instead.
 * License notices are in assets/licenses/prism-orb.txt.
 */
class DadeuriView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    enum class State { IDLE, LISTENING, THINKING, SPEAKING }

    var state = State.IDLE
        set(value) {
            if (field == value) return
            field = value
            retarget()
            invalidate()
        }

    /** While true (driving), motion stops and the visual holds a settled frame (PLEOS-SAFETY-008). */
    var still = false
        set(value) {
            if (field == value) return
            field = value
            invalidate()
        }

    /** Loudness in 0..1, read every frame. While null, the visual follows a simulated voice. */
    var level: (() -> Float)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer = if (Build.VERSION.SDK_INT >= 33) {
        runCatching { GlassRenderer() }
            .onFailure { Log.e(TAG, "Dadeuri shader unavailable", it) }
            .getOrNull()
    } else null
    private var fallback: Shader? = null

    private val seed = Random.nextFloat() * 10f
    private var clock = seed * 3.7f
    private var phase = seed * 5.3f
    private var flowX = 0f
    private var flowY = 0f
    private var spin = 0f
    private var loudness = 0f
    private var warpLoudness = 0f
    private var talk = 0f
    private var lastFrame = 0L

    private val speed = Spring(0.55f)
    private val warp = Spring(0.85f)
    private val swirl = Spring(0f)
    private val scale = Spring(1f)
    private val bright = Spring(0f)
    private val voice = Spring(0f)
    private val ambient = Spring(0f)
    private val breathe = Spring(1f)
    private val inflow = Spring(0f, EFFECT_OMEGA)
    private val bloom = Spring(0f, EFFECT_OMEGA)
    private val flowSpeed = Spring(0.9f, FLOW_OMEGA)
    private val flowAmount = Spring(0.65f, FLOW_OMEGA)
    private val flowDirection = Spring(90f, FLOW_OMEGA)
    private val drift = Spring(0.35f, FLOW_OMEGA)
    private val turbulence = Spring(0.4f, FLOW_OMEGA)
    private val springs = arrayOf(
        speed, warp, swirl, scale, bright, voice, ambient, breathe,
        inflow, bloom, flowSpeed, flowAmount, flowDirection, drift, turbulence,
    )

    private fun retarget() {
        val voiced = state == State.LISTENING || state == State.SPEAKING
        flowSpeed.target = if (voiced) 2.48f else 0.9f
        flowAmount.target = if (voiced) 0.36f else 0.65f
        flowDirection.target = if (voiced) 128f else 90f
        drift.target = if (voiced) 0.37f else 0.35f
        turbulence.target = if (voiced) 0.55f else 0.4f
        ambient.target = if (state == State.LISTENING) 1f else 0f
        voice.target = if (state == State.SPEAKING) 1f else 0f
        inflow.target = ambient.target
        bloom.target = voice.target
        when (state) {
            State.IDLE -> setMotion(0.55f, 0.85f, 0f, 1f, 0f, 1f)
            State.THINKING -> setMotion(2.4f, 1.6f, 0.3f, 0.93f, 0.015f, 0f)
            State.LISTENING, State.SPEAKING -> setMotion(1f, 1f, 0f, 1.025f, 0.035f, 0.3f)
        }
    }

    private fun setMotion(
        speedTo: Float,
        warpTo: Float,
        swirlTo: Float,
        scaleTo: Float,
        brightTo: Float,
        breatheTo: Float,
    ) {
        speed.target = speedTo
        warp.target = warpTo
        swirl.target = swirlTo
        scale.target = scaleTo
        bright.target = brightTo
        breathe.target = breatheTo
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val radius = min(w, h) * FALLBACK_RADIUS
        fallback = if (radius > 0f) RadialGradient(
            w * 0.49f, h * 0.45f, radius * 1.1f,
            intArrayOf(0xFFF6FFFF.toInt(), 0xFFBAEAFB.toInt(), 0xFF799FCA.toInt(), 0xFF304365.toInt()),
            floatArrayOf(0f, 0.28f, 0.57f, 1f), Shader.TileMode.CLAMP,
        ) else null
    }

    private fun nextStep(): Float {
        val now = SystemClock.uptimeMillis()
        val step = if (lastFrame == 0L) DEFAULT_STEP else min((now - lastFrame) / 1000f, MAX_STEP)
        lastFrame = now
        return step
    }

    private fun advance(dt: Float) {
        for (spring in springs) spring.step(dt)
        clock = (clock + dt) % CLOCK_PERIOD
        val gate = min(1f, voice.x + ambient.x)
        val raw = level?.invoke() ?: (
            ambient.x * min(1f, ambientEnvelope(clock, seed) * 1.4f) +
                voice.x * voiceEnvelope(clock, seed)
            )
        loudness = follow(loudness, (raw * REACTIVITY).coerceIn(0f, 1f), dt, 10f, 4f)
        talk = follow(talk, min(1f, loudness * 3f), dt, 6f, 1.2f)
        val hush = 1f - gate * 0.8f * (1f - talk)
        val dPhase = dt * (flowSpeed.x * speed.x * hush + gate * loudness * 1.1f)
        phase = (phase + dPhase) % PHASE_PERIOD
        val direction = Math.toRadians(flowDirection.x.toDouble())
        flowX += cos(direction).toFloat() * drift.x * dPhase
        flowY += sin(direction).toFloat() * drift.x * dPhase
        spin += dt * swirl.x * 1.1f * flowSpeed.x
        warpLoudness = follow(warpLoudness, loudness, dt, 1.5f, 0.8f)
    }

    private fun settle() {
        for (spring in springs) spring.snap()
        loudness = 0f
        warpLoudness = 0f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val side = min(width, height).toFloat()
        if (side <= 0f) return
        val glass = renderer
        if (glass != null && canvas.isHardwareAccelerated) {
            val animate = !still && ValueAnimator.areAnimatorsEnabled()
            if (animate) advance(nextStep()) else settle()
            paint.shader = glass.update(side, animate)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            if (animate && isShown) postInvalidateOnAnimation()
        } else {
            paint.shader = fallback
            canvas.drawCircle(width * 0.5f, height * 0.5f, side * FALLBACK_RADIUS, paint)
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE) invalidate()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == VISIBLE) invalidate()
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        // The animator setting has no change callback before API 33, so re-read it on return.
        if (hasWindowFocus) invalidate()
    }

    @TargetApi(33)
    private inner class GlassRenderer {
        private val shader = RuntimeShader(PRISM_ORB_AGSL).apply {
            setFloatUniform("uColors", SKY_PALETTE)
        }

        fun update(side: Float, animate: Boolean): Shader {
            val breath = if (animate) breathe.x * sin(clock * 1.4f) * 0.012f else 0f
            val gate = min(1f, voice.x + ambient.x)
            shader.setFloatUniform("uCenter", width * 0.5f, height * 0.5f)
            shader.setFloatUniform("uSide", side)
            shader.setFloatUniform("uTime", phase)
            shader.setFloatUniform("uSpin", spin)
            shader.setFloatUniform("uWarp", flowAmount.x * warp.x + warpLoudness * (0.06f + gate * 0.18f))
            shader.setFloatUniform("uTurbulence", turbulence.x)
            shader.setFloatUniform("uScale", scale.x * (1f + loudness * 0.025f + breath + bloom.x * loudness * 0.04f))
            shader.setFloatUniform("uBright", bright.x)
            shader.setFloatUniform("uLevel", loudness)
            shader.setFloatUniform("uFlow", flowX, flowY)
            shader.setFloatUniform("uSeed", seed)
            shader.setFloatUniform("uInflow", max(0f, inflow.x))
            shader.setFloatUniform("uBloom", max(0f, bloom.x))
            shader.setFloatUniform("uClock", clock)
            return shader
        }
    }
}

private class Spring(value: Float, private val omega: Float = 5.5f) {
    private var velocity = 0f
    var x = value
        private set
    var target = value

    fun step(dt: Float) {
        velocity += (omega * omega * (target - x) - 2f * omega * velocity) * dt
        x += velocity * dt
    }

    fun snap() {
        x = target
        velocity = 0f
    }
}

private const val TAG = "DadeuriView"
private const val EFFECT_OMEGA = 6f
private const val FLOW_OMEGA = 7f
private const val REACTIVITY = 1.25f
private const val DEFAULT_STEP = 1f / 60f
private const val MAX_STEP = 1f / 20f
private const val CLOCK_PERIOD = 3600f

/** The noise repeats at this phase for both of its time rates, so wrapping here is seamless. */
private const val PHASE_PERIOD = 13546.875f

/** Half of the shader's ORB_RADIUS, relative to the view's shorter side. */
private const val FALLBACK_RADIUS = 0.43f

private fun oklab(lightness: Float, chroma: Float, hueDegrees: Float): List<Float> {
    val hue = Math.toRadians(hueDegrees.toDouble())
    return listOf(lightness, (chroma * cos(hue)).toFloat(), (chroma * sin(hue)).toFloat())
}

/** OKLab triples, top to bottom. */
private val SKY_PALETTE = (
    oklab(0.62f, 0.18f, 259f) + oklab(0.77f, 0.12f, 244f) +
        oklab(0.96f, 0.02f, 250f) + oklab(0.69f, 0.15f, 252f)
    ).toFloatArray()

private fun noiseHash(n: Float): Float {
    val s = sin(n * 127.1 + 311.7) * 43758.5453
    return (s - floor(s)).toFloat()
}

private fun valueNoise(x: Float): Float {
    val i = floor(x)
    val f = x - i
    val u = f * f * (3f - 2f * f)
    return noiseHash(i) * (1f - u) + noiseHash(i + 1f) * u
}

private fun smooth(from: Float, to: Float, x: Float): Float {
    val t = ((x - from) / (to - from)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

private fun voiceEnvelope(t: Float, seed: Float): Float {
    val phrase = smooth(0.38f, 0.55f, valueNoise(t * 0.55f + seed * 13.1f))
    val syllable = abs(sin(t * Math.PI.toFloat() * (4.2f + valueNoise(t * 0.8f + seed) * 1.6f)))
    val accent = 0.55f + 0.45f * valueNoise(t * 3.1f + seed * 7.7f)
    return phrase * (0.25f + 0.75f * syllable.pow(1.6f)) * accent
}

private fun ambientEnvelope(t: Float, seed: Float): Float {
    val gate = smooth(0.3f, 0.6f, valueNoise(t * 0.7f + seed * 5.3f))
    return gate * (0.2f + 0.5f * valueNoise(t * 5.3f + seed * 3.7f) + 0.3f * valueNoise(t * 11.1f + seed))
}

private fun follow(current: Float, target: Float, dt: Float, attack: Float, release: Float): Float {
    val rate = if (target > current) attack else release
    return current + (target - current) * (1f - exp(-rate * dt))
}

private const val PRISM_ORB_AGSL = """
uniform float2 uCenter;
uniform float uSide;
uniform float3 uColors[4];
uniform float uTime;
uniform float uSpin;
uniform float uWarp;
uniform float uTurbulence;
uniform float uScale;
uniform float uBright;
uniform float uLevel;
uniform float2 uFlow;
uniform float uSeed;
uniform float uInflow;
uniform float uBloom;
uniform float uClock;

const float ORB_RADIUS = 0.86;
const float GRAIN = 0.1;
const float SOFTNESS = 0.4;
const float RIM = 0.15;
const float RIM_SOFTNESS = 0.1;
const float WHITE_SPREAD = 0.1;
const float VIGNETTE = 0.08;
const float DISPERSION = 0.5;
const float TONE_MAP = 0.15;

float4 permute(float4 x) { return mod(((x * 34.0) + 1.0) * x, 289.0); }
float4 taylorInvSqrt(float4 r) { return 1.79284291400159 - 0.85373472095314 * r; }

float snoise(float3 v) {
  const float2 C = float2(1.0 / 6.0, 1.0 / 3.0);
  const float4 D = float4(0.0, 0.5, 1.0, 2.0);
  float3 i = floor(v + dot(v, C.yyy));
  float3 x0 = v - i + dot(i, C.xxx);
  float3 g = step(x0.yzx, x0.xyz);
  float3 l = 1.0 - g;
  float3 i1 = min(g.xyz, l.zxy);
  float3 i2 = max(g.xyz, l.zxy);
  float3 x1 = x0 - i1 + C.xxx;
  float3 x2 = x0 - i2 + C.yyy;
  float3 x3 = x0 - D.yyy;
  i = mod(i, 289.0);
  float4 p = permute(permute(permute(
    i.z + float4(0.0, i1.z, i2.z, 1.0)) +
    i.y + float4(0.0, i1.y, i2.y, 1.0)) +
    i.x + float4(0.0, i1.x, i2.x, 1.0));
  float n_ = 0.142857142857;
  float3 ns = n_ * D.wyz - D.xzx;
  float4 j = p - 49.0 * floor(p * ns.z * ns.z);
  float4 x_ = floor(j * ns.z);
  float4 y_ = floor(j - 7.0 * x_);
  float4 x = x_ * ns.x + ns.yyyy;
  float4 y = y_ * ns.x + ns.yyyy;
  float4 h = 1.0 - abs(x) - abs(y);
  float4 b0 = float4(x.xy, y.xy);
  float4 b1 = float4(x.zw, y.zw);
  float4 s0 = floor(b0) * 2.0 + 1.0;
  float4 s1 = floor(b1) * 2.0 + 1.0;
  float4 sh = -step(h, float4(0.0));
  float4 a0 = b0.xzyw + s0.xzyw * sh.xxyy;
  float4 a1 = b1.xzyw + s1.xzyw * sh.zzww;
  float3 p0 = float3(a0.xy, h.x);
  float3 p1 = float3(a0.zw, h.y);
  float3 p2 = float3(a1.xy, h.z);
  float3 p3 = float3(a1.zw, h.w);
  float4 norm = taylorInvSqrt(float4(dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)));
  p0 *= norm.x; p1 *= norm.y; p2 *= norm.z; p3 *= norm.w;
  float4 m = max(0.6 - float4(dot(x0, x0), dot(x1, x1), dot(x2, x2), dot(x3, x3)), 0.0);
  m = m * m;
  return 42.0 * dot(m * m, float4(dot(p0, x0), dot(p1, x1), dot(p2, x2), dot(p3, x3)));
}

float hash12(float2 p) {
  float3 p3 = fract(float3(p.xyx) * 0.1031);
  p3 += dot(p3, p3.yzx + 33.33);
  return fract((p3.x + p3.y) * p3.z);
}

float2 warpField(float2 q, float f, float t, float2 flow) {
  q -= flow * 1.3;
  float2 w1 = float2(snoise(float3(q * f, t)), snoise(float3(q * f + 7.31, t + 3.17)));
  float2 q2 = q + w1 * uWarp * 0.55;
  float2 w2 = float2(snoise(float3(q2 * f * 1.9 + 1.7, t * 1.4)), snoise(float3(q2 * f * 1.9 - 4.1, t * 1.4 + 9.2)));
  return w1 * 0.55 + w2 * 0.22;
}

float3 oklabToLinear(float3 c) {
  float l_ = c.x + 0.3963377774 * c.y + 0.2158037573 * c.z;
  float m_ = c.x - 0.1055613458 * c.y - 0.0638541728 * c.z;
  float s_ = c.x - 0.0894841775 * c.y - 1.2914855480 * c.z;
  float l = l_ * l_ * l_;
  float m = m_ * m_ * m_;
  float s = s_ * s_ * s_;
  return float3(
     4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s,
    -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s,
    -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s);
}

float3 linearToSrgb(float3 c) {
  c = clamp(c, 0.0, 1.0);
  return mix(12.92 * c, 1.055 * pow(c, float3(1.0 / 2.4)) - 0.055, step(0.0031308, c));
}

float2 voiceWarp(float2 q) {
  float r = length(q) + 0.0001;
  float2 d = q / r;
  float c = smoothstep(0.0, 0.3, r);
  float2 w = d * c * sin(r * 9.0 + uClock * 4.4) * uInflow * (0.012 + uLevel * 0.26);
  w += d * c * sin(r * 9.0 - uClock * 4.4) * uBloom * (0.012 + uLevel * 0.26);
  return w;
}

// AGSL rejects non-constant array indices, so the palette is walked with the loop index.
float3 paletteAt(float u) {
  float x = clamp(u, 0.0, 1.0) * 3.0;
  float3 c = uColors[0];
  for (int i = 0; i < 3; i++) {
    c = mix(c, uColors[i + 1], smoothstep(0.0, 1.0, clamp(x - float(i), 0.0, 1.0)));
  }
  return c;
}

float3 rimPass(float3 lin, float r, float aa) {
  float e = max(1.0 - r, 0.0);
  float v = pow(smoothstep(0.2, 1.0, r), 2.0) * VIGNETTE;
  lin *= 1.0 - v * 0.75;
  float ws = exp(-e / mix(0.015, 0.5, WHITE_SPREAD)) * WHITE_SPREAD;
  lin = mix(lin, float3(1.0), clamp(ws * 0.92, 0.0, 1.0));
  float rimLine = exp(-e / max(mix(0.004, 0.09, RIM_SOFTNESS), aa)) * RIM;
  lin = mix(lin, lin * 1.9 + 0.28, clamp(rimLine, 0.0, 1.0));
  return lin;
}

float2 envSample(float2 s, float2 warp) {
  float2 q = s + warp;
  float by = q.y / mix(0.1, 0.42, SOFTNESS);
  float band = exp(-by * by);
  float glow = exp(-length(q * float2(0.7, 0.95)) * 1.15);
  return float2(band * 0.95 + glow * 1.1, 0.5 - q.y * 0.55);
}

float3 toneMap(float3 c) {
  float peak = max(max(c.r, c.g), c.b);
  const float K = 0.8;
  if (peak <= K) return c;
  float mapped = K + (1.0 - K) * (1.0 - exp(-(peak - K) / (1.0 - K)));
  float3 t = c * (mapped / peak);
  return mix(t, float3(mapped), smoothstep(0.9, 2.6, peak) * 0.7);
}

half4 main(float2 frag) {
  float2 pos = (frag - uCenter) / (uSide * 0.5);
  pos.y = -pos.y;
  float R = ORB_RADIUS * uScale;
  float2 p = pos / R;
  float r2 = dot(p, p);
  float r = sqrt(r2);
  if (r > 1.3) return half4(0.0);
  // AGSL has no fwidth; this is its analytic value for r.
  float aa = 1.25 * (abs(p.x) + abs(p.y)) / max(r, 0.0001) * 2.0 / (uSide * R);

  float inside = 1.0 - smoothstep(1.0 - aa, 1.0 + aa, r);
  float3 n = float3(p, sqrt(max(1.0 - r2, 0.0)));
  float3 I = float3(0.0, 0.0, -1.0);

  float spread = 0.012 + DISPERSION * 0.07;
  float3 eta = 1.0 / (1.36 + float3(-spread, 0.0, spread));
  float lensScale = mix(1.6, 2.8, uTurbulence);
  float3 TR = refract(I, n, eta.r);
  float3 TG = refract(I, n, eta.g);
  float3 TB = refract(I, n, eta.b);
  float2 sR = TR.xy / max(-TR.z, 0.15) * lensScale;
  float2 sG = TG.xy / max(-TG.z, 0.15) * lensScale;
  float2 sB = TB.xy / max(-TB.z, 0.15) * lensScale;

  float2 warp = warpField(sG * 0.6 + float2(uSpin * 0.3, 0.0), mix(0.7, 1.6, uTurbulence), uTime * 0.32, uFlow * 0.32) * uWarp * 0.7
              + voiceWarp(sG * 0.6) * 0.9;
  float2 eR = envSample(sR, warp);
  float2 eG = envSample(sG, warp);
  float2 eB = envSample(sB, warp);
  float boost = 1.0 + uLevel * 0.3 + uBright * 3.0;
  float3 light = oklabToLinear(paletteAt(eG.y)) * float3(eR.x, eG.x, eB.x) * boost;

  float2 average = (uColors[0].yz + uColors[1].yz + uColors[2].yz + uColors[3].yz) / 4.0;
  float3 core = oklabToLinear(float3(0.085, average * 0.45));

  float rimK = min(RIM / 0.3, 1.0);
  float3 rimTint = mix(oklabToLinear(paletteAt(0.2)), float3(1.0), 0.35);
  float fresnel = pow(1.0 - n.z, 3.0);
  float3 rr = r * (1.0 + float3(1.0, 0.0, -1.0) * (0.004 + DISPERSION * 0.012));
  float3 ed = (1.0 - rr) / max(0.03, aa * 2.0);
  float3 edge = exp(-ed * ed) * (1.0 - smoothstep(1.0 - aa, 1.0 + aa, rr));

  float lensFalloff = 1.0 - smoothstep(0.55, 1.0, r) * 0.65;
  float3 col = core + light * lensFalloff * 0.9 + rimTint * (fresnel * 0.7 * mix(0.35, 1.0, rimK) + edge * 0.9 * rimK);
  col *= inside;
  col = rimPass(col, r, aa) * inside;

  float halo = r > 1.0 ? exp(-(r - 1.0) * mix(38.0, 14.0, SOFTNESS)) * 0.45 * rimK : 0.0;
  col += rimTint * halo;
  float alpha = clamp(max(inside, halo), 0.0, 1.0);

  float g = hash12(floor(frag) + uSeed * 97.0) - 0.5;
  col *= 1.0 + g * GRAIN * 0.5;

  float3 straight = alpha > 0.001 ? col / alpha : float3(0.0);
  straight = mix(straight, toneMap(straight), TONE_MAP);
  return half4(linearToSrgb(straight) * alpha, alpha);
}
"""
