<?php
require_once '../vendor/autoload.php';
require_once '../secrets.php';
if (isset($_POST['authkey']) && ($_POST['authkey'] =="abc")){
$stripe = new \Stripe\StripeClient('pk_test_51NxujVD0vuzqQPKcVE6nxzXd9rQGXe1ZaEMjdQpsqiKFL0BcKGaq5WWGKpruBc5aVPnc6mDEk3ck1kEj7zYtOywW00YMW0Kh1n');
// Recibir datos enviados desde la aplicación Android
$name = $_POST['name'];
$address = [
    'line1' => $_POST['line1'],
    'postal_code' => $_POST['postal_code'],
    'city' => $_POST['city'],
    'state' => $_POST['state'],
    'country' => $_POST['country']
];

$amount = $_POST['amount'];  // Monto
$description = $_POST['description'];  // Moneda

// Crear un cliente en Stripe utilizando los datos recibidos
$stripe = new \Stripe\StripeClient($stripeSecretKey);
$customer = $stripe->customers->create([
    'name' => $name,
    'address' => $address
]);

// Puedes realizar más acciones aquí, como generar una clave efímera o crear un PaymentIntent

// Devolver una respuesta al cliente Android (puede ser un JSON de confirmación)
$response = ['message' => 'Cliente creado en Stripe correctamente'];
echo json_encode($response);

// Generar una clave efímera
$ephemeralKey = $stripe->ephemeralKeys->create([
  'customer' => $customer->id,
], [
  'stripe_version' => '2022-08-01',
]);

// Crear un PaymentIntent con el monto y la moneda especificados
$paymentIntent = $stripe->paymentIntents->create([
    'amount' => $amount,  // Utiliza el monto recibido desde la aplicación Android
    'currency' => 'mxn',  // Utiliza la moneda recibida desde la aplicación Android
    'description' => $description,
    'customer' => $customer->id,
    'automatic_payment_methods' => [
        'enabled' => true,
    ],
]);

// Devolver la información en formato JSON
echo json_encode([
  'paymentIntent' => $paymentIntent->client_secret,
  'ephemeralKey' => $ephemeralKey->secret,
  'customer' => $customer->id,
  'publishableKey' => 'pk_test_51NxujVD0vuzqQPKcVE6nxzXd9rQGXe1ZaEMjdQpsqiKFL0BcKGaq5WWGKpruBc5aVPnc6mDEk3ck1kEj7zYtOywW00YMW0Kh1n'
]);

http_response_code(200);
} echo "no autorizado"
?>

