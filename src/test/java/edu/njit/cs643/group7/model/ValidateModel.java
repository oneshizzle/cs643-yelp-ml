package edu.njit.cs643.group7.model;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import edu.njit.cs643.group7.util.Utils;

public class ValidateModel {

	@Test
	public void testBusinessModel() {
		System.out.println("NxL8SIC5yqOdnlXCg18IBg".hashCode());

		Gson gson = new Gson();
		List<Business> businesses = new ArrayList<Business>();

		try (Reader reader = new InputStreamReader(
				ClassLoader.getSystemResourceAsStream("data/sample_business_json"))) {
			JsonReader jsonReader = new JsonReader(reader);
			jsonReader.setLenient(true);
			// Convert JSON to Java Object
			while (jsonReader.hasNext() && jsonReader.peek() != JsonToken.END_DOCUMENT) {
				Business aBusiness = gson.fromJson(jsonReader, Business.class);
				System.out.println(aBusiness.getName() + " " + Utils.isRestaurant(aBusiness));
				businesses.add(aBusiness);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		Assert.assertTrue(businesses.size() == 11);
	}

}
