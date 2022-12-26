package api.pojo;

import java.util.ArrayList;
import java.util.List;

public class Orders {
	private List<String> ingredients = new ArrayList<>();

	public Orders(List<String> ingredients) {
		this.ingredients = ingredients;
	}

	public Orders() {
	}
}
