<?php

$action = $_GET['action'];

switch ($action){
	case "getId":
		echo getId();
		break;
	default:
		echo "Not an action";

}

function getMessage($dialog_id){
	require_once 'connection.php';

	$link = mysqli_connect($host, $user, $password, $database) 
		or die("Wrong password " . mysqli_error($link));
	
	
	
		$query = "SELECT id FROM searching WHERE id=".$id.";";
		$result = mysqli_query($link, $query) or die("Error while running query" . mysqli_error($link)); 
		$row = mysqli_fetch_row($result)[0];

		/*while ($row = mysqli_fetch_row($result)){
							
							//echo $row[0] , " ", $row[1], "<br><br>";
							echo "<option style='color:black; background-color: transparent'> $row[1] </option>";
							
		
						}*/

		if (!$row){
			$ok = True;
		}
	}

	mysqli_close($link); 

	return $id;
}

function getId()
{
	require_once 'connection.php';

	$link = mysqli_connect($host, $user, $password, $database) 
		or die("Wrong password " . mysqli_error($link));
	
	$ok = False;
	$id = -1;

	while (!$ok){
		$id = rand(1, 10000);
		$query = "SELECT id FROM searching WHERE id=".$id.";";
		$result = mysqli_query($link, $query) or die("Error while running query" . mysqli_error($link)); 
		$row = mysqli_fetch_row($result)[0];

		if (!$row){
			$ok = True;
		}
	}

	mysqli_close($link); 

	return $id;
}

?>