db = db.getSiblingDB('adminka');

db.createCollection('students');

db.students.insertOne({
    firstName: "Vitaly",
    lastName: "Irvanev",
    age: 21,
    major: "Student"
});

print('Database "adminka" and collection "students" have been initialized.');