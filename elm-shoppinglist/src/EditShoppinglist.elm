import Browser
import Html exposing (Html, text, pre, div, span, br, input, button, label)
import Html.Attributes exposing (..)
import Html.Events exposing (onInput, onClick)
import Array.Extra exposing(removeAt)
import Http
import Debug exposing (toString)
import Array exposing(Array)
import Json.Decode exposing (Decoder, field, int, nullable, list, string, map2, map3, map4, map5, array, bool)
import Json.Encode.Extra exposing (maybe)
import Json.Encode
import Maybe.Extra exposing (isJust)



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
    , householdId: Int
    , name: String
    , list: Array Item
    , disabled: Bool
    , error: Maybe String
    }

type alias ShoppingList = 
    { name: String
    , id: Int
    , householdId: Int
    , list: Array Item
    , disabled: Bool
    }

type alias Item = 
    { id: Int
    , purchaseId: Maybe Int
    , name: String 
    }


shoppingListDecoder : Decoder ShoppingList
shoppingListDecoder =
  map5 ShoppingList
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
    ( field "disabled" bool)

shoppingListEncoder : ShoppingList -> Json.Encode.Value
shoppingListEncoder shoppingList =
    let encodeItem item = 
            Json.Encode.object 
                [ ("id", Json.Encode.int item.id)
                , ("purchaseId", (maybe Json.Encode.int) item.purchaseId)
                , ("name", Json.Encode.string item.name)
                ]
    in Json.Encode.object
        [ ("name", Json.Encode.string shoppingList.name) 
        , ("list", Json.Encode.array encodeItem shoppingList.list)
        , ("id", Json.Encode.int shoppingList.id)
        , ("householdId", Json.Encode.int shoppingList.householdId)
        , ("disabled", Json.Encode.bool shoppingList.disabled)
        ] 

init : Int -> (Model, Cmd Msg)
init id =
  {-very bad model, fix soon, but how? maybe one case for loaded, one for not loaded-}
  ( { id = id, name = "", householdId = -1 , list = Array.empty, error = Nothing, disabled = False}
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
          ( { model | list = shoppingList.list, name = shoppingList.name, id = shoppingList.id, householdId = shoppingList.householdId}, Cmd.none)
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
        , body = Http.jsonBody (shoppingListEncoder 
          { name = model.name
          , id = model.id
          , householdId = model.householdId
          , list = model.list
          , disabled = model.disabled 
          }
        )
        , expect = Http.expectWhatever Ignore 
        })


-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
  Sub.none



-- VIEW


view : Model -> Html Msg
view model =
      pre [ class " boxMiddle" ] 
        [ label [class "label"] [text "name: "]
        , input [value model.name, onInput NameChanged, class "inputImportant"] [] 
        , br [] []
        , div [] 
            ( model.list 
            |> Array.indexedMap (\index item -> 
                span [] 
                [ input ( List.append (if isJust item.purchaseId then [class "selected"] else []) [value item.name, onInput (ItemNameChanged index), class "numTextInput"]) []
                , button [ onClick (RemoveItem index), class "removeButton"] [text "remove"]
                ]
            ) 
            |> Array.toList
            |> List.intersperse (br [] [])
            )
        , br [] []
        , button [onClick NewItem, class "buttonFixedWidth"] [text "add new item"]
        , button [onClick Save, class "buttonFixedWidth"] [text "save"]
        , case model.error of 
            Just errorMsg -> text (String.concat ["error: ", toString errorMsg])
            Nothing -> text ""
        ]