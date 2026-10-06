package co.edu.uptc.sd.multiservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uptc.sd.multiservice.dto.CalculatorResponseDTO;
import co.edu.uptc.sd.multiservice.service.CalculatorService;

@RestController 
@RequestMapping("api/calculator")
public class CalculatorController {
    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @GetMapping
    public CalculatorResponseDTO calculate(
            @RequestParam double num1,
            @RequestParam double num2,
            @RequestParam String op) {
        return calculatorService.calculate(num1, num2, op);
    }
}
