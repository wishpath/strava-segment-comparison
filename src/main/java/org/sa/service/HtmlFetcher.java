package org.sa.service;

import org.sa.console.Colors;
import org.sa.dto.SegmentDTO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

public class HtmlFetcher {
  public static String fetchSegmentFastestTimeString(SegmentDTO s) throws Exception {
    String dom = fetchSegmentPageDom(s);
    String bestTimeString = dom.split("<td>")[1].split("<abbr")[0];
    System.out.println("FETCHED ALL PEOPLE BEST TIME FROM HTML (STRING): " + bestTimeString);
    if (!bestTimeString.matches("[0-9]{1,2}:[0-9]{2}")) // examples that fit this regex: 9:45, 12:00
        System.out.println(Colors.A4_ORANGE + " WRONG PATTERN OF TIME STRING" + Colors.RESET);
    return bestTimeString;
  }

  public static int fetchSegmentFastestTimeSeconds(SegmentDTO s){
    String timeString = "-1";
    try {
      timeString = fetchSegmentFastestTimeString(s);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    System.out.println("time string: " + timeString + "\n");
    String[] time = timeString.split(":");
    if (time.length == 2)
      return Integer.parseInt(time[0]) * 60 + Integer.parseInt(time[1]);
    else return Integer.parseInt(time[0]);
  }

  public static String fetchSegmentPageDom(SegmentDTO s) throws Exception {
    URL url = new URL("https://www.strava.com/segments/" + s.id);
    System.out.print(s.name + ": " + "FETCHING SEGMENT PAGE DOM: " + url + ": ");
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
    String cookieHeader = "xp_session_identifier=25gut84tv5p; globalHeatmapAboutModal=true; _ga=GA1.1.1334711306.1741812414; _scid=zQTLVlloaddzdz77C8eVk7yZh9KIumbkxr-m0A; _tt_enable_cookie=1; _ttp=01JZ8C04RFTMWGW9PNDRT8K481_.tt.1; sp=6f8bcde8-9428-4438-8b88-968e3f4e19e4; _ScCbts=%5B%22310%3Bchrome.2%3A2%3A5%22%5D; _strava_cbv3=true; _gcl_au=1.1.579443894.1751553938.185127946.1752320362.1752320362; _currentH=d3d3LnN0cmF2YS5jb20=; _strava4_session=3tlo5pginj7c611pso4be3847r5usn7f; fbm_284597785309=base_domain=.www.strava.com; _sp_ses.047d=*; fbsr_284597785309=FGs7GfqGjRCXIiZXzUcs_cEro9_Ep66dGQkwLwNejG4.eyJ1c2VyX2lkIjoiMTAwMDAwMTg1OTczNTkyIiwiY29kZSI6IkFRQzdqS25HM05GdmZ2SUxlRWhTNXdoa2J1YjdpR0M0Nm9Gd25tVGlfaEFPWkhZRzFXWUl6UF92aFlRU2tCU0pnMzh3d0xLQjhyQ3RzN3Y0U25tQVdTQ3RMQ2RraWttclkzN1p3aEpXSkRpa1lxU1Q1QWRSV0hVeEItekNtVmRVOGZpSDlSTU8zaGNFTFRjS2VYZ3FNMVdVMUNWTkc3VzQtR1BfN2ZhbnFmWUMwY3hvb1dFWFRxQ2Zra05jZW1tN1FEZnJqX1JRZ1l3RzFpOUx5ZVJvZFRtVnAtUWtvSjFOeGxlSzVUeUdHZVlUcjFscWhqQ204SDg5aW9rblczU1I2a1A1SU54c29pOEgwbUdNTXltaWlqMnJaT0RjRUN5dDNOOFNiY050dFdoMExlVGhhV1IwNUEwek5xRWFXS3Rha1ZzdXpUejlSRDAtczFjT0RKQXpEd21GNDdJLU5ZWjhWdDdTYUhldWxyVXlyZyIsIm9hdXRoX3Rva2VuIjoiRUFBQUFRa05aQWt0MEJPN2VmMDRvU1FhN0VYSE80enpDOGRueW12SEJpbHRWejdIeG1qNFZpN2Y1a0hHWkNWcWdaQXRaQ2pBenp5UmtXWkJsdGtiS3ZKejhUcjExSFJ2VVMwTUowdVlkUGNaQURWY3VpcllKQ0ZZVjhYclI4aGRKVnhuRHZoUGg0TjYxYlpBWkJaQzk0RTZHbjJ2bTBHYW9sVEh0Ykl0NE5neVIxVVhNT2trSFRjOGphcXZjMjJNVXpBWXZqTzFodlFLcXcxQ01aRCIsImFsZ29yaXRobSI6IkhNQUMtU0hBMjU2IiwiaXNzdWVkX2F0IjoxNzUyNDAwMTE1fQ; _ga_12345=GS2.1.s1752399828$o52$g1$t1752401113$j60$l0$h23295301; fbsr_284597785309=aapAPChWiMs-PTlHA7xJ7h2YE8yN2MQ6Lnr5f5o284M.eyJ1c2VyX2lkIjoiMTAwMDAwMTg1OTczNTkyIiwiY29kZSI6IkFRQnhDMHhPWnkwNkVQcDh1NVphWVpkdDM4aUxSbVc1UG5qY0ZNckFTZkhDLXRkcmdoUVJhUFNBMmFieVZZdk16emZtb1BGQjZHcGZKMkFKazE3T08wdmxfd2wteHdZT2ZXaDlVZ2RfUVVvaFZrQTRjVjdVTGo2U0haT3hUODBGaGxEUVM2YU5YNmROSU5obnlNb1VsMU13bjlpVHdTdWJ1SzJVbkFmbEthX3lUOERaSm92RGdYbDI5b3RhSU5xS0hlR010bkRuSkdKYnYwM1NaSzQ4em5HQWNLaG5YNl9WVVJvbDV3bl9oc3cyRVBxTzJwa0Z6V1dFc0lqNGhIS3ZjbG11Slc1dTZTNXpaNmFxckkzdS1GTDQ2LWE3YkNsSGFiU29sRlFaNWRUN2dpX3I4cDZ3SDNacGFfMExVWGU1dEFrQ0ZZNlJEeHpRR3FVand4dUxEUnRCT0Y0Tmd5eDNKRWh1MzRpMm5XVnY0USIsIm9hdXRoX3Rva2VuIjoiRUFBQUFRa05aQWt0MEJPN2VmMDRvU1FhN0VYSE80enpDOGRueW12SEJpbHRWejdIeG1qNFZpN2Y1a0hHWkNWcWdaQXRaQ2pBenp5UmtXWkJsdGtiS3ZKejhUcjExSFJ2VVMwTUowdVlkUGNaQURWY3VpcllKQ0ZZVjhYclI4aGRKVnhuRHZoUGg0TjYxYlpBWkJaQzk0RTZHbjJ2bTBHYW9sVEh0Ykl0NE5neVIxVVhNT2trSFRjOGphcXZjMjJNVXpBWXZqTzFodlFLcXcxQ01aRCIsImFsZ29yaXRobSI6IkhNQUMtU0hBMjU2IiwiaXNzdWVkX2F0IjoxNzUyMzk4NDcwfQ; _scid_r=zATLVlloaddzdz77C8eVk7yZh9KIumbkxr-ngw; _ga_ESZ0QKJW56=GS2.1.s1752397971$o132$g1$t1752401116$j57$l0$h0; ttcsid=1752397975092::EQx4yj6zGLJt0ja2Z4ph.48.1752401116802; ttcsid_CRCAPDJC77UE5B95LUQG=1752397975089::AJuo6erZsCC7NnLaogbz.50.1752401117012; _sp_id.047d=e90babe5-18ce-45bc-9e68-3d12838a274d.1741812402.132.1752402174.1752393386.620d8aa6-fefc-472e-8a39-3529b00713b6\n";
    String escapedCookieHeader = cookieHeader.replaceAll("[\\r\\n]+", "").trim();
    conn.setRequestProperty("Cookie", escapedCookieHeader);

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
      String dom =  reader.lines().collect(Collectors.joining("\n"));
      return dom;
    }
  }
}
