# Onkyo Auto connects to a compatible onkyo receiver and allows a user to send commands to said receiver via a restful api.
At this time the the QSTN commands will not work since the application sends and receives commands/responses on separate threads.
to resolve this issue see the HalEndPoint (https://github.com/mlouis5/HalEndPoint.git) project for a websocket solution, which 
makes use of this api to send non QSTN commands to the receiver, and can continually update the front end. I can't take all the
credit on this since I did make use of other projects to get the back end working as quickly as possible. will provide credit soon.

See: http://tom.webarts.ca/Blog/new-blog-items/javaeiscp-integraserialcontrolprotocol
See: 

These two projects are still in the works, and has a front end which has not yet been placed on github. further refactoring 
may be neccesary. will update this space as needed. please fork and modify as needed.
