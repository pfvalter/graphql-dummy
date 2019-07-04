## GraphQL dummy project

I've derived this code from the 'HowToGraph - GraphQL with Sangria Tutorial' codebase which can be seen here: [HowtoGraphql.com](http://howtographql.com).

### TODO:

- Next steps are to extrapolate Authorization to the HTTP level and use headers instead of the weird looking authentication that is being done using the middleware layer.
-- Maybe I can keep using the MyContext to store session variables (such as the user) BUT I don't want it being handler inside the GraphQL 'Executor' layer.

- Another next step is to use DI for the database and have the config come from somewhere else other than hardcoded. This is needed for next step too.

- Last step is to add proper testing. I have no clue on what is there to test GraphQL but, considering there is an HTTP layer on top, probably any test aproach used to test another Akka HTTP app is reusable here too.


