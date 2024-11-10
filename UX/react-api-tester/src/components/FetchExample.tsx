import { useState, useEffect } from "react";
import MyButton from "./MyButton";

export default function FetchExample() {
  // 2. Create our *dogImageUrl* variable as well as the *setDogImageUrl* function via useState
  // We're setting the default value of dogImageUrl to null, so that while we wait for the
  // fetch to complete, we dont attempt to render the image
  let [dogImageUrl, setDogImageUrl] = useState(null);
  let [refreshSignal, setRefreshSignal] = useState(true);
  const refreshOnClick = () => {
    setRefreshSignal(!refreshSignal);
  };

  // 3. Create out useEffect function
  useEffect(() => {
    fetch("https://dog.ceo/api/breeds/image/random")
      .then((response) => response.json())
      // 4. Setting *dogImageUrl* to the image url that we received from the response above
      .then((data) => {
        console.log("FETCH GOT US: " + data.message);
        setDogImageUrl(data.message);
      });
  }, [refreshSignal]);

  // returns:
  // {"message":"https:\/\/images.dog.ceo\/breeds\/schnauzer-giant\/n02097130_3452.jpg","status":"success"}
  return (
    <>
      <MyButton onClick={refreshOnClick}>Refresh the doggie</MyButton>
      {dogImageUrl && <img src={dogImageUrl}></img>}
    </>
  );
}
