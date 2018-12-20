const server = require('../server');
const ds = server.dataSources.base_db;
const lbTables = ['ZUser', 'ACL', 'RoleMapping', 'Role', 'Image'];

ds.automigrate(lbTables, function (er) {
  if (er) throw er;
  console.log('Loopback tables [' + lbTables + '] created in ', ds.adapter.name);

  populateUsers(ds);

  ds.disconnect();
});

function populateUsers (app) {
  console.log("Migration : make-default_users")
  var User = app.models.ZUser;
  var Role = app.models.Role;
  var Image = app.models.Image;
  var RoleMapping = app.models.RoleMapping;

  User.create([
    {username: 'John', email: 'test1@example.com', password: '1234'},
    {username: 'Jane', email: 'test2@example.com', password: '1234'},
    {username: 'Bob', email: 'test3@example.com', password: '1234'}
  ], function(err, users) {
    if (err) throw err;

    console.log('Created users:', users);

    Image.create([
      { userId : users[0].id, path : users[0].id + "/test.jpeg", name : "test.jpeg", accessType : 0}
    ], function(err, images) {
      console.log('Created images:', images);
    });

    //create the admin role
    Role.create({
      name: 'admin'
    }, function(err, role) {
      if (err) throw err;

      console.log('Created role:', role);

      //make bob an admin
      role.principals.create({
        principalType: RoleMapping.USER,
        principalId: users[2].id
      }, function(err, principal) {
        if (err) throw err;

        console.log('Created principal:', principal);
      });
    });
  });
};
