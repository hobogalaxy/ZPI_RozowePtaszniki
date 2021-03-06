package app;
import app.util.CalculatorResult;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;

public class ProfitCalculator {

    private ArrayList<TaxesInState> taxesInStates;

    ProfitCalculator() {
        try {
            taxesInStates = Database.getTaxesFromDB();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String CalculateForAllStates(String id, String category, String basePrice, String finalPrice) {

        System.out.println(id);
        System.out.println(category);
        System.out.println(basePrice);
        System.out.println(finalPrice);

        if(id == null || finalPrice == null || basePrice == null || category == null)
            throw new IllegalArgumentException();

        int idInt = Integer.parseInt(id);
        double basePriceDouble = Double.parseDouble(basePrice);
        double finalPriceDouble = Double.parseDouble(finalPrice);

        ArrayList<ProfitData> listOfProfits = new ArrayList<>();
        for (TaxesInState val : taxesInStates) {
            double baseTax;
            try {
                String taxStr = val.getTaxForCategory(category);
                if(taxStr == null) {
                    System.out.println("Category doesn't exist");
                    throw new IllegalArgumentException();
                }
                baseTax = parseTaxValue(taxStr, finalPrice, val.getBaseTax());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                throw new IllegalArgumentException();
            }

            double finalPriceWithoutTaxes = Math.round(finalPriceDouble / (1.0 + baseTax / 100.0) * 100.0) / 100.0;
            listOfProfits.add(new ProfitData(
                    val.getName(),
                    finalPriceWithoutTaxes,
                    Math.round((finalPriceWithoutTaxes - basePriceDouble - val.getLogisticCost()) * 100.0) / 100.0,
                    val.getLogisticCost()
            ));
        }

        CalculatorResult result = new CalculatorResult(idInt, basePriceDouble, finalPriceDouble, listOfProfits);
        String jsonResult = result.getResult();
        System.out.println(jsonResult);
        return jsonResult;
    }

    private Double parseTaxValue(String input, String finalPrice, Double baseTax) {
        double result = 0;
        if(input.substring(0,1).equals("$")) {
            result = Double.parseDouble(input.substring(1));

            if(result < Double.parseDouble(finalPrice)) {
                result = baseTax;
            }
            else
                result = 0;
        }
        else
            result = Double.parseDouble(input);

        return result;
    }

}
