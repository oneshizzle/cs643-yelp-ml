package edu.njit.cs643.group7.util;

import java.util.Arrays;

import edu.njit.cs643.group7.model.Business;

public class Utils {

	public static Boolean isRestaurant(Business aBusiness) {
		Boolean isRestaurant = false;
		if (aBusiness.getCategories() == null)
			return isRestaurant;
		for (String category : aBusiness.getCategories()) {
			if (Arrays.asList("food", "restaurants").contains(category.toLowerCase()))
				isRestaurant = true;
		}
		return isRestaurant;
	}

}
