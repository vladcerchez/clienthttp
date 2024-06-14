import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Scanner;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Main {
    private static final String URL = "https://localhost:5001/api/Category/categories/";
    private static final String URL_CATEGORY = "https://localhost:5001/api/Category/";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1. Display the list of categories");
            System.out.println("2. Display details about a category");
            System.out.println("3. Create a new category");
            System.out.println("4. Delete a category");
            System.out.println("5. Rename a category");
            System.out.println("6. Add a product to a category");
            System.out.println("7. Display products in a category");
            System.out.println("8. Exit");
            System.out.print("\nEnter the option number: ");
            String option = scanner.nextLine();

            disableCertificateValidation();
            switch (option) {
                case "1":
                    displayCategories();
                    break;
                case "2":
                    displayCategoryDetails();
                    break;
                case "3":
                    createNewCategory();
                    break;
                case "4":
                    deleteCategory();
                    break;
                case "5":
                    renameCategory();
                    break;
                case "6":
                    addProductToCategory();
                    break;
                case "7":
                    displayProductsInCategory();
                    break;
                case "8":
                    System.out.println("Program terminated.");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void displayCategories() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                Scanner scanner = new Scanner(connection.getInputStream());
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();

                JSONArray categoriesArray = new JSONArray(response.toString()); // parse String JSON for creating a data structure JSON.
                System.out.println("List of categories:");   // creating object JSONArray from String obtained from response.toString().
                for (int i = 0; i < categoriesArray.length(); i++) {
                    JSONObject category = categoriesArray.getJSONObject(i);
                    System.out.println("ID: " + category.getInt("id") + " - Name: " + category.getString("name"));
                }
            } else {
                System.out.println("Failed to retrieve the list.");
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static void displayCategoryDetails() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the category ID to see details: ");
        String category_id = scanner.nextLine();
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(URL + category_id).openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                Scanner responseScanner = new Scanner(connection.getInputStream());
                while (responseScanner.hasNextLine()) {
                    response.append(responseScanner.nextLine());
                }
                responseScanner.close();

                JSONArray categoryArray = new JSONArray(response.toString());
                if (categoryArray.length() > 0) {
                    JSONObject category = categoryArray.getJSONObject(0);
                    System.out.println("\nDetails about the category:");
                    System.out.println("ID: " + category.getInt("id"));
                    System.out.println("Name: " + category.getString("name"));
                    System.out.println("Number of items: " + category.getInt("itemsCount"));
                } else {
                    System.out.println("Category not found.");
                }
            } else {
                System.out.println("Failed to retrieve category information");
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createNewCategory() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the name of the new category: ");
        String category_name = scanner.nextLine();
        try {
            JSONObject newCategory = new JSONObject();
            newCategory.put("title", category_name);

            HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = newCategory.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                Scanner responseScanner = new Scanner(connection.getInputStream());
                while (responseScanner.hasNextLine()) {
                    response.append(responseScanner.nextLine());
                }
                responseScanner.close();
                JSONObject jsonResponse = new JSONObject(response.toString());
                System.out.println("\nCategory " + jsonResponse.getString("name") + " has been created");
            } else {
                System.out.println("Failed to create category");
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static void deleteCategory() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the ID of the category you want to delete: ");
        String category_id = scanner.nextLine();
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(URL + category_id).openConnection();
            connection.setRequestMethod("DELETE");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("\nCategory with ID " + category_id + " has been deleted");
            } else {
                System.out.println("Failed to delete category");
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void renameCategory() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the ID of the category you want to modify: ");
        String category_id = scanner.nextLine();
        System.out.print("Enter the new name for the selected category: ");
        String new_name = scanner.nextLine();
        try {
            JSONObject modifiedCategory = new JSONObject();
            modifiedCategory.put("title", new_name);

            HttpURLConnection connection = (HttpURLConnection) new URL(URL_CATEGORY + category_id).openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = modifiedCategory.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                Scanner responseScanner = new Scanner(connection.getInputStream());
                while (responseScanner.hasNextLine()) {
                    response.append(responseScanner.nextLine());
                }
                responseScanner.close();
                JSONObject jsonResponse = new JSONObject(response.toString());
                System.out.println("\nCategory with ID " + category_id + " has been successfully modified!");
                System.out.println("The new name of the category is " + jsonResponse.getString("name"));
            } else {
                System.out.println("Failed to modify category");
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addProductToCategory() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the ID of the category where you want to add a product: ");
        String category_id = scanner.nextLine();
        System.out.print("Enter the title of the product: ");
        String product_title = scanner.nextLine();
        System.out.print("Enter the price of the product: ");
        String product_price = scanner.nextLine();
        try {
            JSONObject newProduct = new JSONObject();
            newProduct.put("title", product_title);
            newProduct.put("price", Integer.parseInt(product_price));
            newProduct.put("categoryId", Integer.parseInt(category_id));

            HttpURLConnection connection = (HttpURLConnection) new URL(URL + category_id + "/products").openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = newProduct.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                Scanner responseScanner = new Scanner(connection.getInputStream());
                while (responseScanner.hasNextLine()) {
                    response.append(responseScanner.nextLine());
                }
                responseScanner.close();
                JSONObject jsonResponse = new JSONObject(response.toString());
                System.out.println("\nProduct with title " + jsonResponse.getString("title") + " has been successfully added");
            } else {
                System.out.println("Failed to add the product to the category");
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    public static void disableCertificateValidation() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void displayProductsInCategory() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the ID of the category to display the list of products: ");
        String category_id = scanner.nextLine();
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(URL + category_id + "/products").openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                Scanner responseScanner = new Scanner(connection.getInputStream());
                while (responseScanner.hasNextLine()) {
                    response.append(responseScanner.nextLine());
                }
                responseScanner.close();

                JSONArray productsArray = new JSONArray(response.toString());
                System.out.println("List of products in the category with ID " + category_id + ":");
                for (int i = 0; i < productsArray.length(); i++) {
                    JSONObject product = productsArray.getJSONObject(i);
                    System.out.println("Name: " + product.getString("title") + " - Price: " + product.getInt("price") + " Lei");
                }
            } else {
                System.out.println("Failed to retrieve the list of products for the category");
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
