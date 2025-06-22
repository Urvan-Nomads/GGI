package urbanNomadServer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import urbanNomadServer.service.RouteService;

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
    public String done(Model model) {
        routeService.done();
        model.addAttribute("locations", routeService.getWaypoints());
        return "result";
    }

}
