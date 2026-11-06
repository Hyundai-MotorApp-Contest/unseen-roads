package io.github.hyundaimotorappcontest.unseen

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

class MainActivity : Activity() {
    private var screen = Screen.HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shell)
        findViewById<View>(R.id.prism_radio).setOnClickListener { show(Screen.RADIO) }
        findViewById<View>(R.id.prism_story).setOnClickListener { show(Screen.CAPTURE) }


        findViewById<Button>(R.id.welcome_action).setOnClickListener {
            show(
                when (screen) {
                    Screen.RADIO_PAUSED -> Screen.RADIO
                    Screen.RADIO -> Screen.RADIO_PAUSED
                    Screen.CAPTURE -> Screen.REVIEW
                    Screen.REVIEW -> Screen.DONE
                    Screen.HOME, Screen.DONE -> Screen.HOME
                },
            )
        }
        findViewById<Button>(R.id.secondary_action).setOnClickListener { show(Screen.HOME) }
        findViewById<Button>(R.id.tertiary_action).setOnClickListener {
            show(Screen.HOME)
        }
        show(screen)
    }

    private fun show(next: Screen) {
        screen = next
        val showFlow = next != Screen.HOME
        findViewById<View>(R.id.prism_home).visibility = if (next == Screen.HOME) View.VISIBLE else View.GONE
        findViewById<View>(R.id.story_flow).visibility = if (showFlow) View.VISIBLE else View.GONE
        if (!showFlow) return

        findViewById<TextView>(R.id.screen_label).setText(screen.label)
        findViewById<TextView>(R.id.welcome_title).setText(screen.title)
        findViewById<TextView>(R.id.welcome_description).setText(screen.description)
        findViewById<Button>(R.id.welcome_action).setText(screen.action)
        findViewById<Button>(R.id.secondary_action).apply {
            val secondary = screen.secondary
            visibility = if (secondary == null) View.GONE else View.VISIBLE
            secondary?.let { setText(it) }
        }
        findViewById<Button>(R.id.tertiary_action).visibility =
            if (screen == Screen.RADIO || screen == Screen.RADIO_PAUSED) View.VISIBLE else View.GONE
        findViewById<TextView>(R.id.orb_status).setText(screen.status)
        findViewById<PrismOrbView>(R.id.flow_orb).state = when (next) {
            Screen.RADIO -> PrismOrbView.State.SPEAKING
            Screen.CAPTURE -> PrismOrbView.State.LISTENING
            Screen.REVIEW -> PrismOrbView.State.THINKING
            else -> PrismOrbView.State.IDLE
        }
    }

    private enum class Screen(
        val label: Int,
        val title: Int,
        val description: Int,
        val action: Int,
        val status: Int = R.string.orb_demo,
        val secondary: Int? = null,
    ) {
        HOME(R.string.app_name, R.string.prism_title, R.string.prism_status, R.string.back_to_home),
        RADIO(
            R.string.radio_label, R.string.radio_title, R.string.radio_description,
            R.string.radio_pause, R.string.orb_reading,
        ),
        RADIO_PAUSED(
            R.string.radio_label, R.string.radio_title, R.string.radio_description,
            R.string.radio_resume, R.string.orb_paused,
        ),
        CAPTURE(
            R.string.demo_label, R.string.demo_capture_title, R.string.demo_capture_description,
            R.string.demo_finish, R.string.orb_listening, R.string.demo_cancel,
        ),
        REVIEW(
            R.string.demo_label, R.string.demo_review_title, R.string.demo_review_description,
            R.string.demo_keep, R.string.orb_route, R.string.demo_cancel,
        ),
        DONE(
            R.string.demo_label, R.string.demo_done_title, R.string.demo_done_description,
            R.string.back_to_home,
        ),
    }
}
