package com.inha.everytown.domain.etc.crawling.service;

import com.inha.everytown.domain.place.dto.PlaceMenuDto;
import com.inha.everytown.domain.place.entity.document.PlaceDoc;
import com.inha.everytown.domain.place.service.document.PlaceDocService;
import com.inha.everytown.domain.place.service.relation.PlaceMenuService;
import com.inha.everytown.domain.restaurant.dto.RestaurantMenuDto;
import com.inha.everytown.domain.restaurant.entity.document.RestaurantDoc;
import com.inha.everytown.domain.restaurant.service.document.RestaurantDocService;
import com.inha.everytown.domain.restaurant.service.relation.RestaurantMenuService;
import com.inha.everytown.global.exception.CustomException;
import com.inha.everytown.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CrawlingService {

    private final KakaoLocalApiService kakaoLocalApiService;
    private final RestaurantMenuService restaurantMenuService;
    private final RestaurantDocService restaurantDocService;
    private final PlaceMenuService placeMenuService;
    private final PlaceDocService placeDocService;
    private final String WEB_DRIVER_ID = "webdriver.chrome.driver";
    private final String WEB_DRIVER_PATH = "C:/Users/user/project/everytown/spring/everytown/src/main/resources/driver/chromedriver.exe";
    private final WebDriver driver;

    @Autowired
    public CrawlingService(KakaoLocalApiService kakaoLocalApiService,
                           RestaurantMenuService restaurantMenuService,
                           RestaurantDocService restaurantDocService,
                           PlaceMenuService placeMenuService,
                           PlaceDocService placeDocService) {

        this.kakaoLocalApiService = kakaoLocalApiService;
        this.restaurantMenuService = restaurantMenuService;
        this.restaurantDocService = restaurantDocService;
        this.placeMenuService = placeMenuService;
        this.placeDocService = placeDocService;

        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);
        options.addArguments("--start-maximized");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--remote-allow-origins=*");
        options.setCapability("ignoreProtectedModeSettings", true);

        this.driver = new ChromeDriver(options);
    }
    public List<RestaurantMenuDto> getRestaurantMenuData(Long restaurantId) {

        List<RestaurantMenuDto> menuList = new ArrayList<>();

        RestaurantDoc restaurantDoc = restaurantDocService.getRestaurantDoc(restaurantId);    // DB보단 elastic이 더 빨라서 이걸 사용
        // 카카오 Id로 크롤링 하기 위함
        Long kakaoPlaceId = kakaoLocalApiService.getKakaoId(restaurantDoc.getName(), restaurantDoc.getLocation().getLat(), restaurantDoc.getLocation().getLon());
        // Id == 0 -> 데이터 못 찾았을 경우 -> 응답 데이터를 null이 아니라 빈 배열로 보내기 위함
        if(kakaoPlaceId == 0) return menuList;

        String url = "https://place.map.kakao.com/m/" + kakaoPlaceId + "#menuInfo";
        System.out.println(url);

        try {
            driver.get(url);
            Thread.sleep(500);

            List<WebElement> nameElements = driver.findElements(By.className("menu_g"));
            List<WebElement> priceElements = driver.findElements(By.className("price_menu"));

            // menu 이름이 있는 항목이 2가지 존재
            if (nameElements.size() == 0) {
                nameElements = driver.findElements(By.className("name_menu"));
            }

            // 여기까지 했는데 size가 0이면 메뉴 정보 없는 것. -> 그냥 빈 배열로 저장
            if (nameElements.size() == 0) {
                return menuList;
            }

            for (int i = 0; i < nameElements.size(); i++) {
                String name = nameElements.get(i).getText();
                // 6,000 -> 6000 등 formatting 위함. 또한 메뉴 정보는 있어도 가격은 없는 경우 존재
                Integer price = i < priceElements.size() ? Integer.parseInt(priceElements.get(i).getText().replaceAll(",", "")) : null;
                RestaurantMenuDto menu = RestaurantMenuDto.builder()
                        .name(name)
                        .price(price)
                        .build();
                menuList.add(menu);
            }

            return menuList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.CRAWLING_ERROR);
        }
    }

    public List<PlaceMenuDto> getPlaceMenuData(Long placeId) {

        List<PlaceMenuDto> menuList = new ArrayList<>();

        PlaceDoc placeDoc = placeDocService.getPlaceDoc(placeId);    // DB보단 elastic이 더 빨라서 이걸 사용
        // 카카오 Id로 크롤링 하기 위함
        Long kakaoPlaceId = kakaoLocalApiService.getKakaoId(placeDoc.getName(), placeDoc.getLocation().getLat(), placeDoc.getLocation().getLon());
        // Id == 0 -> 데이터 못 찾았을 경우 -> 응답 데이터를 null이 아니라 빈 배열로 보내기 위함
        if(kakaoPlaceId == 0) return menuList;

        String url = "https://place.map.kakao.com/m/" + kakaoPlaceId + "#menuInfo";
        System.out.println(url);

        try {
            driver.get(url);
            Thread.sleep(500);

            List<WebElement> nameElements = driver.findElements(By.className("menu_g"));
            List<WebElement> priceElements = driver.findElements(By.className("price_menu"));

            // menu 이름이 있는 항목이 2가지 존재
            if (nameElements.size() == 0) {
                nameElements = driver.findElements(By.className("name_menu"));
            }

            // 여기까지 했는데 size가 0이면 메뉴 정보 없는 것. -> 그냥 빈 배열로 저장
            if (nameElements.size() == 0) {
                return menuList;
            }

            for (int i = 0; i < nameElements.size(); i++) {
                String name = nameElements.get(i).getText();
                // 6,000 -> 6000 등 formatting 위함. 또한 메뉴 정보는 있어도 가격은 없는 경우 존재
                Integer price = i < priceElements.size() ? Integer.parseInt(priceElements.get(i).getText().replaceAll(",", "")) : null;
                PlaceMenuDto menu = PlaceMenuDto.builder()
                        .name(name)
                        .price(price)
                        .build();
                menuList.add(menu);
            }

            return menuList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.CRAWLING_ERROR);
        }
    }

    public String getRestaurantImage(Long restaurantId) {
        RestaurantDoc restaurantDoc = restaurantDocService.getRestaurantDoc(restaurantId);    // DB보단 elastic이 더 빨라서 이걸 사용
        // 카카오 Id로 크롤링 하기 위함
        Long kakaoPlaceId = kakaoLocalApiService.getKakaoId(restaurantDoc.getName(), restaurantDoc.getLocation().getLat(), restaurantDoc.getLocation().getLon());
        // Id == 0 -> 데이터 못 찾았을 경우 -> 다시 크롤링 안하게 쓰래기 값 넣는다
        if(kakaoPlaceId == 0) return "NO_IMAGE";

        String url = "https://place.map.kakao.com/m/" + kakaoPlaceId + "#photoList?pidx=0";
        System.out.println(url);

        try {
            driver.get(url);
            Thread.sleep(500);

            WebElement element = driver.findElement(By.className("box_photo"));

            if (element == null) {
                return "NO_IMAGE";
            }

            String style = element.getAttribute("style");
            int startIndex = style.indexOf("url(\"");
            int endIndex = style.indexOf("\")", startIndex);
            String img = style.substring(startIndex + 5, endIndex);
            System.out.println(img);

            return img;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.CRAWLING_ERROR);
        }
    }

    public String getPlaceImage(Long placeId) {
        PlaceDoc placeDoc = placeDocService.getPlaceDoc(placeId);    // DB보단 elastic이 더 빨라서 이걸 사용
        // 카카오 Id로 크롤링 하기 위함
        Long kakaoPlaceId = kakaoLocalApiService.getKakaoId(placeDoc.getName(), placeDoc.getLocation().getLat(), placeDoc.getLocation().getLon());
        // Id == 0 -> 데이터 못 찾았을 경우 -> 다시 크롤링 안하게 쓰래기 값 넣는다
        if(kakaoPlaceId == 0) return "NO_IMAGE";

        String url = "https://place.map.kakao.com/m/" + kakaoPlaceId + "#photoList?pidx=0";
        System.out.println(url);

        try {
            driver.get(url);
            Thread.sleep(500);

            WebElement element = driver.findElement(By.className("box_photo"));

            if (element == null) {
                return "NO_IMAGE";
            }

            String style = element.getAttribute("style");
            int startIndex = style.indexOf("url(\"");
            int endIndex = style.indexOf("\")", startIndex);
            String img = style.substring(startIndex + 5, endIndex);
            System.out.println(img);

            return img;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.CRAWLING_ERROR);
        }
    }
}