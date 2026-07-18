export type User = {
  id: string
  email: string
  name: string
  pictureUrl?: string
}

export type ExpenseCategory = 'NEEDS' | 'WANTS' | 'INVESTMENT' | 'SAVINGS' | 'OTHER'

export type Expense = {
  id: string
  title: string
  amount: number
  category: ExpenseCategory
  note?: string
  spentOn: string
}

export type FinanceProfile = {
  id?: string
  monthlySalary: number
  idealNeedsPercent: number
  idealWantsPercent: number
  idealInvestPercent: number
  idealSavingsPercent: number
  goalAmount: number
  goalLabel: string
}

export type SpendingFlag = {
  title: string
  message: string
  actual: number
  ideal: number
  severity: string
}

export type FinanceOverview = {
  profile: FinanceProfile
  expenses: Expense[]
  spentByCategory: Record<ExpenseCategory, number>
  idealNeedsAmount: number
  idealWantsAmount: number
  idealInvestAmount: number
  idealSavingsAmount: number
  totalSpent: number
  remaining: number
  goalProgressPercent: number
  flags: SpendingFlag[]
  advice: string[]
}

export type CareerItemType = 'GOAL' | 'TODO' | 'LEARN'

export type CareerItem = {
  id: string
  type: CareerItemType
  title: string
  description?: string
  status?: string
  priority?: number
  dueDate?: string
  completed: boolean
}

export type TravelPage = {
  id: string
  title: string
  icon?: string
  content: string
  tags?: string[]
  archived?: boolean
  updatedAt?: string
}

export type Reminder = {
  id: string
  subject: string
  body: string
  sendAt: string
  sent: boolean
  errorMessage?: string
}
