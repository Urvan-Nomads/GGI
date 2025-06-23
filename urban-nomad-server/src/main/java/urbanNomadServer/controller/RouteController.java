package urbanNomadServer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import urbanNomadServer.service.RouteService;

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class RouteController {

    private final RouteService routeService;
    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping("/departure")
    public ResponseEntity<?> getDeparture(@RequestParam String address) {
        routeService.setDeparture(address);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/waypoint")
    public ResponseEntity<?> getWaypoints(@RequestParam String contentId) {
        routeService.addWaypointByContentId(Integer.parseInt(contentId));
        return ResponseEntity.ok(null);
    }

    @GetMapping("/done")
    public String done(Model model) throws JsonProcessingException {
        routeService.greedy(); // 기존 done 에서 greedy 로 rename. (해당 함수는 경로만 재계산함)
        routeService.excludeDeparture();

        model.addAttribute("departure", routeService.getDeparture());
        model.addAttribute("locations", routeService.getWaypoints());

        routeService.makeFullWaypoints();
        // JSON 문자열로 변환해서 넘기기
        ObjectMapper objectMapper = new ObjectMapper();
        String locationsJson = objectMapper.writeValueAsString(routeService.getFullWaypoints());
        System.out.println("===FullWaypoints===");
        System.out.println(routeService.getFullWaypoints()) ;
        model.addAttribute("locationsJson", locationsJson);

        return "result";
    }

}
