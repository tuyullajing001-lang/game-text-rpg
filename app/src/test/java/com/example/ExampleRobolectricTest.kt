package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.generator.HeroFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Infinite Gacha", appName)
  }

  @Test
  fun `gacha generates valid hero with stats`() {
    val result = HeroFactory.rollGacha("gold")
    assertNotNull(result.hero)
    assertTrue(result.hero.starGrade in 1..5)
    assertTrue(result.hero.maxHp > 0)
    assertTrue(result.hero.physicalAtk > 0)
    assertTrue(result.hero.fatigue == 0)
    assertTrue(result.hero.stress == 0)
  }
}

