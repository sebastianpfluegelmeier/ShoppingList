import Browser
import Html exposing (Html, text, pre, div, span, br, input, button)
import Html.Attributes exposing (..)
import Html.Events exposing (onInput, onClick)
import Array.Extra exposing(removeAt)
import Http
import Debug exposing (toString)
import Array exposing(Array)
import Json.Decode exposing (Decoder, field, int, nullable, list, string, map2, map3, map4, array)
import Json.Encode



-- MAIN


main =
  Browser.element
    { init = init
    , update = update
    , subscriptions = subscriptions
    , view = view
    }



-- MODEL


type alias Model = 
    { id: Int
    , shoppingListId: Int
    , name: String
    , list: Array Item
    , error: Maybe String
    }

type alias ShoppingList = 
    { name: String
    , id: Int
    , shoppingListId: Int
    , list: Array Item
    }

type alias Item = 
    { id: Int
    , purchaseId: Maybe Int
    , name: String 
    }


shoppingListDecoder : Decoder ShoppingList
shoppingListDecoder =
  map4 ShoppingList
    ( field "name" string )
    ( field "id" int )
    ( field "householdId" int )
    ( field "list" 
      ( array
        ( map3 Item
            (field "id" int)
            (field "purchaseId" (nullable int))
            (field "name" string)
        )
      )
    )

shoppingListEncoder : ShoppingList -> Json.Encode.Value
shoppingListEncoder shoppingList =
    let encodeItem item = 
            Json.Encode.object 
                [ ("id", Json.Encode.int item.id)
                , ("name", Json.Encode.string item.name)
                ]
    in Json.Encode.object
        [ ("name", Json.Encode.string shoppingList.name) 
        , ("list", Json.Encode.array encodeItem shoppingList.list)
        ] 

init : Int -> (Model, Cmd Msg)
init id =
  {-very bad model, fix soon, but how? maybe one case for loaded, one for not loaded-}
  ( { id = id, name = "", shoppingListId = -1 , list = Array.empty, error = Nothing}
  , Http.get
      { url = String.concat ["/shoppingListJson/", String.fromInt id]
      , expect = Http.expectJson GotShoppingList shoppingListDecoder
      }
  )



-- UPDATE


type Msg
  = GotShoppingList (Result Http.Error ShoppingList)
  | NameChanged String
  | ItemNameChanged Int String
  | NewItem
  | RemoveItem Int
  | Save
  | Ignore (Result Http.Error ())


update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    GotShoppingList result ->
      case result of
        Ok shoppingList ->
          ( { model | list = shoppingList.list, name = shoppingList.name }, Cmd.none)
        Err error ->
          ( {model | error = Just (toString error) }, Cmd.none)
    NameChanged name -> ( { model | name = name }  , Cmd.none)
    ItemNameChanged index name -> 
        let newItem = 
                Array.get index model.list 
                |> Maybe.withDefault { id = 0, purchaseId = Nothing , name = name } 
                |> \item -> { item | name = name}
        in ( {model | list = Array.set index newItem model.list }, Cmd.none)
    NewItem -> ( {model | list = Array.push {id = 0, purchaseId = Nothing, name = "" } model.list }, Cmd.none)
    RemoveItem index -> ( {model | list = removeAt index model.list }, Cmd.none)
    Ignore _ -> (model, Cmd.none)
    Save -> (model, Http.post 
        { url = String.concat ["/shoppingListJson/", String.fromInt model.id]
        , body = Http.jsonBody (shoppingListEncoder { name = model.name, id = model.id, shoppingListId = model.shoppingListId, list = model.list })
        , expect = Http.expectWhatever Ignore 
        })


-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
  Sub.none



-- VIEW


view : Model -> Html Msg
view model =
      pre [] 
        [ text "name: ", input [value model.name, onInput NameChanged] [] 
        , br [] []
        , div [] 
            ( model.list 
            |> Array.indexedMap (\index item -> 
                span [] 
                [ input [value item.name, onInput (ItemNameChanged index)] []
                , button [ onClick (RemoveItem index)] [text "remove"]
                ]
            ) 
            |> Array.toList
            |> List.intersperse (br [] [])
            )
        , br [] []
        , button [onClick NewItem] [text "add new item"]
        , button [onClick Save] [text "save"]
        , case model.error of 
            Just errorMsg -> text (String.concat ["error: ", toString errorMsg])
            Nothing -> text ""
        ]