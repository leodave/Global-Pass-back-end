DELETE
FROM PRODUCTS;

INSERT INTO PRODUCTS (id, name, description, page_link, login_username, login_password, amount, currency, other_details) VALUES
        ('prod-001', 'Netflix Standard', 'Streaming service with HD quality and 2 screens', 'https://netflix.com/login', 'user1@email.com', 'NetPass123!', 15.99, 'USD', 'Renews monthly on the 1st'),
        ('prod-002', 'Spotify Premium', 'Ad-free music streaming with offline downloads', 'https://spotify.com/login', 'user1@email.com', 'SpotPass456!', 9.99, 'USD', 'Family plan - 6 accounts'),
        ('prod-003', 'Adobe Creative Cloud', 'Full suite of Adobe design tools', 'https://adobe.com/login', 'work@company.com', 'AdobePass789!', 54.99, 'USD', 'Annual plan, next renewal Jan 2026'),
        ('prod-004', 'GitHub Pro', 'Advanced GitHub features for developers', 'https://github.com/login', 'devuser123', 'GithubPass321!', 4.00, 'USD', 'Includes private repos and Actions minutes'),
        ('prod-005', 'NordVPN', 'VPN service with 6 device connections', 'https://nordvpn.com/login', 'vpnuser@email.com', 'NordPass654!', 4.99, 'USD', '2 year plan expires Dec 2026'),
        ('prod-006', 'ChatGPT Plus', 'OpenAI GPT-4 access with priority access', 'https://chat.openai.com/login', 'aiuser@email.com', 'OpenAIPass987!', 20.00, 'USD', 'Renews on the 15th each month'),
        ('prod-007', 'Microsoft 365', 'Office apps with 1TB OneDrive storage', 'https://microsoft.com/login', 'msuser@outlook.com', 'MSPass147!', 9.99, 'USD', 'Personal plan, 5 devices'),
        ('prod-008', 'Notion Pro', 'Productivity and note-taking workspace', 'https://notion.so/login', 'notionuser@email.com', 'NotionPass258!', 8.00, 'USD', 'Unlimited AI responses included'),
        ('prod-009', 'AWS Lightsail', 'Cloud hosting for personal projects', 'https://aws.amazon.com/login', 'awsdev@email.com', 'AWSPass369!', 3.50, 'USD', 'Nairobi region, 1GB RAM instance'),
        ('prod-010', 'Figma Professional', 'UI/UX design tool with team features', 'https://figma.com/login', 'designuser@email.com', 'FigmaPass741!', 12.00, 'USD', 'Seat license, annual billing'),
        ('prod-011', 'IELTS Registration', 'IELTS Academic exam registration fee', 'https://ielts.org/book', '<username>', '<password>', 255.00, 'USD', 'Select nearest test center and preferred date'),
        ('prod-012', 'TOEFL Registration', 'TOEFL iBT exam registration fee', 'https://ets.org/toefl', '<username>', '<password>', 245.00, 'USD', 'Book at least 2 weeks before test date'),
        ('prod-013', 'University Application Fee', 'Standard university application fee payment', 'https://apply.university.edu', '<username>', '<password>', 75.00, 'USD', 'Provide university portal login and application ID'),
        ('prod-014', 'Udemy Course', 'Online course purchase on Udemy', 'https://udemy.com', '<username>', '<password>', 19.99, 'USD', 'Provide exact course link in description'),
        ('prod-015', 'Coursera Plus', 'Coursera annual subscription for unlimited courses', 'https://coursera.org', '<username>', '<password>', 59.00, 'USD', 'Annual plan with certificate access');
