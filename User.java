class User {
    private final int id;
    private final String name;
    private final int age;
    private final String gender;
    public User(int id, String name, int age, String gender) {
        this.id = id; this.name = name; this.age = age; this.gender = gender; }
    public User(String line) {
        String[] parts = line.split("\t");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid user line: " + line);
        }
        this.id = Integer.parseInt(parts[0].trim());
        this.name = parts[1].trim();
        this.age = Integer.parseInt(parts[2].trim());
        this.gender = parts[3].trim();
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    @Override
    public String toString() {
        return "User{id=%d, name=%s, age=%d, gender=%s}"
                .formatted(id, name, age, gender);
    }
}
