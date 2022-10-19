package com.tignioj.entity;

public class AppSetting {
   private boolean showWeather;
   private boolean showSecond;
   private boolean screenAlwaysOn;
   private boolean nightMode;
   private String cityName;
   private String cityCode;
   private String heWeatherAPIKey;

   // 字体
   public int clockTextSize;
   public int countDownTextSize;
   public int eventTextSize;
   public int noteTextSize;

   public AppSetting() {
   }


   public String getHeWeatherAPIKey() {
      return heWeatherAPIKey;
   }

   public void setHeWeatherAPIKey(String heWeatherAPIKey) {
      this.heWeatherAPIKey = heWeatherAPIKey;
   }

   public AppSetting(boolean showWeather, boolean showSecond, boolean screenAlwaysOn,
                     boolean nightMode, String cityName, String cityCode,
                     int clockTextSize, int countDownTextSize, int eventTextSize, int noteTextSize, String heWeatherAPIKey) {
      this.showWeather = showWeather;
      this.showSecond = showSecond;
      this.screenAlwaysOn = screenAlwaysOn;
      this.nightMode = nightMode;
      this.cityName = cityName;
      this.cityCode = cityCode;
      this.clockTextSize = clockTextSize;
      this.countDownTextSize = countDownTextSize;
      this.eventTextSize = eventTextSize;
      this.noteTextSize = noteTextSize;
      this.heWeatherAPIKey = heWeatherAPIKey;
   }

   public boolean isShowWeather() {
      return showWeather;
   }

   public String getCityCode() {
      return cityCode;
   }

   public void setCityCode(String cityCode) {
      this.cityCode = cityCode;
   }

   public void setShowWeather(boolean showWeather) {
      this.showWeather = showWeather;
   }

   public boolean isShowSecond() {
      return showSecond;
   }

   public void setShowSecond(boolean showSecond) {
      this.showSecond = showSecond;
   }

   public String getCityName() {
      return cityName;
   }

   public void setCityName(String cityName) {
      this.cityName = cityName;
   }

   public boolean isScreenAlwaysOn() {
      return screenAlwaysOn;
   }

   public void setScreenAlwaysOn(boolean screenAlwaysOn) {
      this.screenAlwaysOn = screenAlwaysOn;
   }

   public boolean isNightMode() {
      return nightMode;
   }

   public void setNightMode(boolean nightMode) {
      this.nightMode = nightMode;
   }
}
