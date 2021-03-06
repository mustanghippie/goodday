package goodday;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Created by Nobu on 2017/04/27.
 * <p>
 * Using json-lib
 * http://json-lib.sourceforge.net/
 */
public class OpenWeatherMapAPI {

    private String apiKey = "";
    private GoodDayModel gdm = new GoodDayModel();

    public OpenWeatherMapAPI() {
        // Prepare open weather map API key
        String keyValue = gdm.fileReaderFunction("src/key");
        this.apiKey = keyValue;
    }

    /**
     * Obtains weather information.
     *
     * @return
     * @author Alex
     */
    public HashMap<String, HashMap<String, String>> openWeatherMap() {

        // Gets latitude 緯度 longitude 経度
        String lat = gdm.getUserData().get("lat");
        String lon = gdm.getUserData().get("lon");

        //String requestURL = "http://api.openweathermap.org/data/2.5/forecast?id=" + cityID + "&APPID=" + this.apiKey;
        String requestURL = "http://api.openweathermap.org/data/2.5/forecast?lat=" +lat +"&lon=" +lon +"&APPID=" + this.apiKey;
        // Result weather information from API
        String data;
        String line; // one line from API result or a file

        HashMap<String, HashMap<String, String>> allWeatherData = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        // Flag witch allows connecting API or not
        boolean allowingConnectionFlag = getAllowingConnectionFlag();

        if (allowingConnectionFlag) { // Read data from API
            URL url = null;
            try {
                url = new URL(requestURL);
                InputStream is = url.openConnection().getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.out.println("URL error@OpenWeatherMapAPI");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("IOException@OpenWeatherMapAPI:code1");
            }
            data = sb.toString();
        } else { // Data read from a local file
            data = gdm.fileReaderFunction("src/weatherInformation.json");

        }

        // Convert JSON
        JSONObject jsonObject = JSONObject.fromObject(data);
        JSONArray listArray = jsonObject.getJSONArray("list");

        HashMap<String, String> nowWeatherData = new HashMap<>();
        HashMap<String, String> in3WeatherData = new HashMap<>();
        HashMap<String, String> in6WeatherData = new HashMap<>();
        HashMap<String, String> in9WeatherData = new HashMap<>();
        JSONObject jObject;

        for (int i = 0; i < 4; i++) {
            jObject = listArray.getJSONObject(i);

            String weather = convertWeatherSimply(jObject.getJSONArray("weather").getJSONObject(0).getString("main"));
            String temp = getTemperature(jObject.getJSONObject("main").getDouble("temp"));
            // Calculates time from time stamp
            long unixTimeStamp = Long.parseLong(jObject.getString("dt"));
            java.util.Date conversionTime = new java.util.Date(unixTimeStamp * 1000);

            // Sets time zone
            TimeZone tz = TimeZone.getTimeZone(gdm.getUserData().get("timeZone"));
            SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.CANADA);
            format.setTimeZone(tz);
            String time = format.format(conversionTime);

            String windCondition = convertWindConditionName(jObject.getJSONObject("wind").getDouble("speed"));

            switch (i) {
                case 0:
                    nowWeatherData.put("weather", weather);
                    nowWeatherData.put("temp", temp);
                    nowWeatherData.put("time", time);
                    nowWeatherData.put("windCondition", windCondition);
                    break;
                case 1:
                    in3WeatherData.put("weather", weather);
                    in3WeatherData.put("temp", temp);
                    in3WeatherData.put("time", time);
                    in3WeatherData.put("windCondition", windCondition);
                    break;
                case 2:
                    in6WeatherData.put("weather", weather);
                    in6WeatherData.put("temp", temp);
                    in6WeatherData.put("time", time);
                    in6WeatherData.put("windCondition", windCondition);
                    break;
                case 3:
                    in9WeatherData.put("weather", weather);
                    in9WeatherData.put("temp", temp);
                    in9WeatherData.put("time", time);
                    in9WeatherData.put("windCondition", windCondition);
                    break;
                default:
                    System.out.println("Ouf of index");
            }
        }

        allWeatherData.put("now", nowWeatherData);
        allWeatherData.put("in3", in3WeatherData);
        allWeatherData.put("in6", in6WeatherData);
        allWeatherData.put("in9", in9WeatherData);

        // Save weather information(file)
        if (allowingConnectionFlag) {
            File file = new File("src/weatherInformation.json");
            try {
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fileWriter);
                PrintWriter pw = new PrintWriter(bw);

                pw.println(jsonObject.toString(4));
                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("IOException@OpenWeatherMapAPI:code3");
            }
        }

        return allWeatherData;
    }

    /**
     * Converts wind speed to wind condition.
     *
     * @author Nobu
     * @param windSpeed
     * @return wind condition
     */
    private String convertWindConditionName(double windSpeed) {

        if (windSpeed <= 0.3) return "Calm";
        if (windSpeed <= 1.5) return "LightAir";
        if (windSpeed <= 3.3) return "LightBreeze";
        if (windSpeed <= 5.5) return "GentleBreeze";
        if (windSpeed <= 7.9) return "ModerateBreeze";
        if (windSpeed <= 10.7) return "FreshBreeze";
        if (windSpeed <= 13.8) return "StrongBreeze";

        return "HighWind";
    }

    /**
     * Converts weather name from open weather map API's name.
     *
     * @param weather
     * @return String
     * @author Nobu
     */
    private String convertWeatherSimply(String weather) {
        String simpleWeather = "";

        if (simpleWeather == null) System.out.println("Null Error");

        switch (weather) {
            case "Thunderstorm":
                simpleWeather = "Thunderstorm";
                break;
            case "Drizzle":
                simpleWeather = "Drizzle";
                break;
            case "Rain":
                simpleWeather = "Rainy";
                break;
            case "Snow":
                simpleWeather = "Snowy";
                break;
            case "Atmosphere":
                simpleWeather = "Cloudy";
                break;
            case "Clear":
                simpleWeather = "Sunny";
                break;
            case "Clouds":
                simpleWeather = "Cloudy";
                break;
            case "Extreme":
                simpleWeather = "Cloudy";
                break;
            case "Additional":
                simpleWeather = "Cloudy";
                break;
            default:
                System.out.println("Out of Group code");
        }

        return simpleWeather;
    }

    /**
     * Checks user data for unit user chose
     * uses Celcius temp as a base and from there calculates Farenheit
     *
     * @return temp C or F
     * @author Alex
     */
    private String getTemperature(double baseTemp) {

        String unit = gdm.getUserData().get("unit"); // checking which unit user chose C or F
        String temp;

        if (unit.equals("C°")) {
            temp = String.valueOf(Math.round(baseTemp - 273.15f));
            return temp;
        } else {
            temp = String.valueOf(Math.round(baseTemp - 273.15f) * 9 / 5 + 32);
            return temp;
        }
    }

    /**
     * If 10 minutes has past after this program connect to API, return true.
     * Check when weatherInformation file has modified
     *
     * @return boolean true => allows connecting API, false => not allow connecting API
     * @throws IOException file read error, a user use this APP for the first time
     * @author Juria
     */
    private boolean getAllowingConnectionFlag() {

        Long lastModified;
        long currentTime;
        long timeDiff;

        try {
            File file = new File("src/weatherInformation.json");
            FileReader fileReader = new FileReader(file);

            // File time when it made
            lastModified = file.lastModified();

            // Check 10 mins
            currentTime = System.currentTimeMillis();
            timeDiff = currentTime - lastModified;

            if (timeDiff < 600000) return false;
            else return true;

        } catch (IOException e) {
            return true;
        }

    }
}
